#!/bin/bash

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"
DB_NAME="${DB_NAME:-notifications}"
DB_USER="${DB_USER:-postgres}"
DB_PASSWORD="${DB_PASSWORD:-}"
FLYWAY_ACTION="${1:-migrate}" # migrate, info, validate, repair

# Logging functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

validate_environment() {
    log_info "Validating environment..."

    # Check required tools
    command -v psql >/dev/null 2>&1 || { log_error "PostgreSQL client (psql) is required but not installed"; exit 1; }
    command -v mvn >/dev/null 2>&1 || { log_error "Maven is required but not installed"; exit 1; }

    # Check database password
    if [ -z "$DB_PASSWORD" ]; then
        log_error "Database password is required. Set DB_PASSWORD environment variable."
        exit 1
    fi

    # Export password for psql
    export PGPASSWORD="$DB_PASSWORD"
}

wait_for_database() {
    log_info "Waiting for database to be ready..."

    local max_attempts=30
    local attempt=1

    while [ $attempt -le $max_attempts ]; do
        if psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -c '\q' 2>/dev/null; then
            log_success "Database is ready!"
            return 0
        fi

        log_info "Database is unavailable - attempt $attempt/$max_attempts - sleeping 5 seconds..."
        sleep 5
        attempt=$((attempt + 1))
    done

    log_error "Database is not ready after $max_attempts attempts"
    exit 1
}

check_database_connection() {
    log_info "Checking database connection..."

    if ! psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -c 'SELECT version();' >/dev/null 2>&1; then
        log_error "Cannot connect to database $DB_NAME at $DB_HOST:$DB_PORT"
        log_error "Please check your database configuration and credentials"
        exit 1
    fi

    log_success "Database connection successful"
}

create_database_if_not_exists() {
    log_info "Checking if database exists..."

    if ! psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -c '\q' 2>/dev/null; then
        log_warning "Database $DB_NAME does not exist, attempting to create..."

        # Connect to postgres database to create new database
        if psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "postgres" -c "CREATE DATABASE $DB_NAME;" 2>/dev/null; then
            log_success "Database $DB_NAME created successfully"
        else
            log_error "Failed to create database $DB_NAME"
            exit 1
        fi
    else
        log_success "Database $DB_NAME already exists"
    fi
}

check_flyway_metadata() {
    log_info "Checking Flyway metadata..."

    if psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -c "SELECT table_name FROM information_schema.tables WHERE table_name = 'flyway_schema_history';" | grep -q "flyway_schema_history"; then
        log_success "Flyway metadata table exists"
        return 0
    else
        log_warning "Flyway metadata table does not exist (this is normal for new databases)"
        return 1
    fi
}

run_flyway_action() {
    local action="$1"
    local flyway_command="flyway:$action"

    log_info "Running Flyway $action..."

    case $action in
        "migrate")
            log_info "Applying database migrations..."
            ;;
        "info")
            log_info "Showing migration information..."
            ;;
        "validate")
            log_info "Validating migrations..."
            ;;
        "repair")
            log_info "Repairing Flyway metadata..."
            ;;
        "clean")
            log_warning "WARNING: This will destroy all database objects!"
            read -p "Are you sure you want to continue? (y/N): " -n 1 -r
            echo
            if [[ ! $REPLY =~ ^[Yy]$ ]]; then
                log_info "Clean operation cancelled"
                exit 0
            fi
            ;;
    esac

    # Run Flyway command
    if ./mvnw -q "$flyway_command" \
        -Dflyway.url="jdbc:postgresql://$DB_HOST:$DB_PORT/$DB_NAME" \
        -Dflyway.user="$DB_USER" \
        -Dflyway.password="$DB_PASSWORD" \
        -Dflyway.locations="filesystem:src/main/resources/db/migration"; then
        log_success "Flyway $action completed successfully"
    else
        log_error "Flyway $action failed"
        exit 1
    fi
}

show_migration_info() {
    log_info "Current migration status:"

    psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" << EOF
SELECT
    installed_rank,
    version,
    description,
    type,
    script,
    installed_by,
    installed_on,
    execution_time,
    success
FROM flyway_schema_history
ORDER BY installed_rank;
EOF
}

show_database_info() {
    log_info "Database information:"

    echo "Database: $DB_NAME"
    echo "Host: $DB_HOST:$DB_PORT"
    echo "User: $DB_USER"

    # Show table counts
    log_info "Table statistics:"
    psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -c "
    SELECT
        table_name,
        (SELECT count(*) FROM notifications.\"\" || table_name || \"\") as row_count
    FROM information_schema.tables
    WHERE table_schema = 'public'
    AND table_type = 'BASE TABLE'
    ORDER BY table_name;" 2>/dev/null || log_warning "Could not retrieve table statistics"
}

backup_database() {
    local backup_dir="./backups"
    local timestamp=$(date +%Y%m%d_%H%M%S)
    local backup_file="backup_${DB_NAME}_${timestamp}.sql"

    log_info "Creating database backup..."

    mkdir -p "$backup_dir"

    if pg_dump -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" > "$backup_dir/$backup_file"; then
        log_success "Backup created: $backup_dir/$backup_file"

        # Compress backup
        gzip "$backup_dir/$backup_file"
        log_success "Backup compressed: $backup_dir/$backup_file.gz"

        # Clean up old backups (keep last 7)
        find "$backup_dir" -name "backup_${DB_NAME}_*.sql.gz" -type f | sort -r | tail -n +8 | xargs rm -f
    else
        log_error "Backup failed!"
        exit 1
    fi
}

main() {
    log_info "Starting database migration process"
    log_info "Database: $DB_NAME, Host: $DB_HOST:$DB_PORT, Action: $FLYWAY_ACTION"

    # Validate environment
    validate_environment

    # Wait for database
    wait_for_database

    # Check connection
    check_database_connection

    # Create database if needed
    create_database_if_not_exists

    # Create backup before destructive operations
    if [ "$FLYWAY_ACTION" = "migrate" ] || [ "$FLYWAY_ACTION" = "clean" ]; then
        backup_database
    fi

    # Run Flyway action
    run_flyway_action "$FLYWAY_ACTION"

    # Show migration info for migrate action
    if [ "$FLYWAY_ACTION" = "migrate" ]; then
        show_migration_info
        show_database_info
    fi

    log_success "Database migration process completed successfully! ✅"
}

# Handle script interruption
trap 'log_error "Migration interrupted"; exit 1' INT TERM

# Run main function
main "$@"

# Cleanup
unset PGPASSWORD
