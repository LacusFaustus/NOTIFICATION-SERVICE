#!/bin/bash

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
APP_NAME="notification-service"
VERSION="${1:-1.0.0}"
DOCKER_REGISTRY="${DOCKER_REGISTRY:-your-registry}"
NAMESPACE="${NAMESPACE:-notification}"
ENVIRONMENT="${ENVIRONMENT:-production}"
KUBE_CONTEXT="${KUBE_CONTEXT:-production-cluster}"

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

# Validation functions
validate_environment() {
    log_info "Validating environment..."

    # Check required tools
    command -v docker >/dev/null 2>&1 || { log_error "Docker is required but not installed"; exit 1; }
    command -v kubectl >/dev/null 2>&1 || { log_error "kubectl is required but not installed"; exit 1; }
    command -v mvn >/dev/null 2>&1 || { log_error "Maven is required but not installed"; exit 1; }

    # Check Kubernetes context
    if ! kubectl config current-context >/dev/null 2>&1; then
        log_error "No Kubernetes context configured"
        exit 1
    fi

    log_info "Current Kubernetes context: $(kubectl config current-context)"
}

check_dependencies() {
    log_info "Checking cluster dependencies..."

    # Check if namespace exists
    if ! kubectl get namespace "$NAMESPACE" >/dev/null 2>&1; then
        log_warning "Namespace $NAMESPACE does not exist, creating..."
        kubectl create namespace "$NAMESPACE"
    fi

    # Check for required secrets
    if ! kubectl get secret -n "$NAMESPACE" notification-service-secrets >/dev/null 2>&1; then
        log_error "Required secret 'notification-service-secrets' not found in namespace $NAMESPACE"
        log_error "Please create the secret using: kubectl apply -f k8s/secret.yaml"
        exit 1
    fi
}

build_application() {
    log_info "Building application..."

    # Run tests
    log_info "Running tests..."
    if ! ./mvnw clean test; then
        log_error "Tests failed! Aborting deployment."
        exit 1
    fi

    # Build JAR
    log_info "Building JAR file..."
    if ! ./mvnw clean package -DskipTests; then
        log_error "Build failed!"
        exit 1
    fi

    log_success "Application built successfully"
}

build_docker_image() {
    log_info "Building Docker image..."

    local image_tag="$DOCKER_REGISTRY/$APP_NAME:$VERSION"
    local latest_tag="$DOCKER_REGISTRY/$APP_NAME:latest"

    # Build image
    if ! docker build -t "$image_tag" .; then
        log_error "Docker build failed!"
        exit 1
    fi

    # Tag as latest
    docker tag "$image_tag" "$latest_tag"

    log_success "Docker image built: $image_tag"
}

push_docker_image() {
    log_info "Pushing Docker image to registry..."

    local image_tag="$DOCKER_REGISTRY/$APP_NAME:$VERSION"
    local latest_tag="$DOCKER_REGISTRY/$APP_NAME:latest"

    # Check if we're logged in to registry
    if ! docker info | grep -q "$DOCKER_REGISTRY"; then
        log_warning "Not logged in to $DOCKER_REGISTRY, attempting to login..."
        if [ -n "$DOCKER_USERNAME" ] && [ -n "$DOCKER_PASSWORD" ]; then
            echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin "$DOCKER_REGISTRY"
        else
            log_error "Docker registry credentials not provided"
            exit 1
        fi
    fi

    # Push images
    if ! docker push "$image_tag"; then
        log_error "Failed to push image $image_tag"
        exit 1
    fi

    if ! docker push "$latest_tag"; then
        log_error "Failed to push image $latest_tag"
        exit 1
    fi

    log_success "Docker images pushed successfully"
}

deploy_kubernetes() {
    log_info "Deploying to Kubernetes..."

    # Apply configuration in order
    log_info "Applying Kubernetes manifests..."

    # Namespace (if not exists)
    kubectl apply -f k8s/namespace.yaml

    # ConfigMap
    kubectl apply -f k8s/configmap.yaml -n "$NAMESPACE"

    # Secret (should already exist, but verify)
    if [ -f "k8s/secret.yaml" ]; then
        kubectl apply -f k8s/secret.yaml -n "$NAMESPACE"
    fi

    # Deployment
    log_info "Updating deployment image..."
    kubectl set image deployment/"$APP_NAME" "$APP_NAME=$DOCKER_REGISTRY/$APP_NAME:$VERSION" -n "$NAMESPACE" --record

    # Wait for rollout
    log_info "Waiting for rollout to complete..."
    if ! kubectl rollout status deployment/"$APP_NAME" -n "$NAMESPACE" --timeout=600s; then
        log_error "Deployment rollout failed!"

        # Show rollout history
        log_info "Rollout history:"
        kubectl rollout history deployment/"$APP_NAME" -n "$NAMESPACE"

        # Show pod status for debugging
        log_info "Current pod status:"
        kubectl get pods -n "$NAMESPACE" -l app="$APP_NAME"

        exit 1
    fi

    log_success "Kubernetes deployment completed successfully"
}

run_health_checks() {
    log_info "Running health checks..."

    local retries=30
    local count=0

    # Get service URL
    local service_url=$(kubectl get service -n "$NAMESPACE" "$APP_NAME" -o jsonpath='{.status.loadBalancer.ingress[0].ip}')
    if [ -z "$service_url" ]; then
        service_url=$(kubectl get service -n "$NAMESPACE" "$APP_NAME" -o jsonpath='{.status.loadBalancer.ingress[0].hostname}')
    fi

    if [ -z "$service_url" ]; then
        log_warning "Cannot determine service URL, using port-forward for health check"
        # Use port-forward for health check
        kubectl port-forward -n "$NAMESPACE" deployment/"$APP_NAME" 8080:8080 &
        local pf_pid=$!
        sleep 5

        while [ $count -lt $retries ]; do
            if curl -s -f http://localhost:8080/actuator/health >/dev/null 2>&1; then
                kill $pf_pid
                log_success "Health check passed"
                return 0
            fi
            count=$((count + 1))
            sleep 10
            log_info "Health check attempt $count/$retries..."
        done

        kill $pf_pid
    else
        # Use actual service URL
        while [ $count -lt $retries ]; do
            if curl -s -f "http://$service_url/actuator/health" >/dev/null 2>&1; then
                log_success "Health check passed"
                return 0
            fi
            count=$((count + 1))
            sleep 10
            log_info "Health check attempt $count/$retries..."
        done
    fi

    log_error "Health check failed after $retries attempts"
    return 1
}

cleanup() {
    log_info "Cleaning up..."

    # Remove local Docker images to save space
    docker rmi "$DOCKER_REGISTRY/$APP_NAME:$VERSION" 2>/dev/null || true
    docker rmi "$DOCKER_REGISTRY/$APP_NAME:latest" 2>/dev/null || true

    log_info "Cleanup completed"
}

main() {
    log_info "Starting deployment of $APP_NAME version $VERSION to $ENVIRONMENT"
    log_info "Registry: $DOCKER_REGISTRY, Namespace: $NAMESPACE"

    # Execute deployment steps
    validate_environment
    check_dependencies
    build_application
    build_docker_image
    push_docker_image
    deploy_kubernetes
    run_health_checks
    cleanup

    log_success "Deployment completed successfully! 🚀"

    # Show deployment info
    log_info "Deployment information:"
    kubectl get deployment,svc,pods -n "$NAMESPACE" -l app="$APP_NAME"
}

# Handle script interruption
trap 'log_error "Deployment interrupted"; exit 1' INT TERM

# Run main function
main "$@"
