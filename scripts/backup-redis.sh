#!/bin/bash

set -e

# Configuration
BACKUP_DIR="./backups"
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="redis_backup_$DATE.rdb"

echo "Starting Redis backup..."

# Create backup directory if it doesn't exist
mkdir -p "$BACKUP_DIR"

# Check if Redis is running
if ! redis-cli ping > /dev/null 2>&1; then
    echo "Error: Redis is not running or not accessible"
    exit 1
fi

# Create backup
echo "Creating Redis backup..."
redis-cli SAVE

# Find Redis data directory
REDIS_DIR=$(redis-cli config get dir | tail -n 1)
DUMP_FILE="$REDIS_DIR/dump.rdb"

if [ ! -f "$DUMP_FILE" ]; then
    echo "Error: Redis dump file not found at $DUMP_FILE"
    exit 1
fi

# Copy backup file
cp "$DUMP_FILE" "$BACKUP_DIR/$BACKUP_FILE"

# Compress backup
echo "Compressing backup..."
gzip "$BACKUP_DIR/$BACKUP_FILE"

# Clean up old backups (keep last 30 days)
find "$BACKUP_DIR" -name "redis_backup_*.rdb.gz" -mtime +30 -delete

echo "Redis backup completed: $BACKUP_DIR/$BACKUP_FILE.gz"

# Display backup information
echo "Backup size: $(du -h "$BACKUP_DIR/$BACKUP_FILE.gz" | cut -f1)"
echo "Available backups:"
ls -la "$BACKUP_DIR"/redis_backup_*.rdb.gz
