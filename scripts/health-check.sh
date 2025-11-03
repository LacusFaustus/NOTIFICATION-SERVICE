#!/bin/bash

set -e

# Configuration
SERVICE_URL="http://localhost:8080"
MAX_ATTEMPTS=30
ATTEMPT=0

echo "Waiting for notification service to be healthy..."

while [ $ATTEMPT -lt $MAX_ATTEMPTS ]; do
    if curl -s -f "$SERVICE_URL/actuator/health" > /dev/null 2>&1; then
        echo "Service is healthy!"
        exit 0
    fi

    echo "Attempt $((ATTEMPT + 1))/$MAX_ATTEMPTS failed. Retrying in 5 seconds..."
    sleep 5
    ATTEMPT=$((ATTEMPT + 1))
done

echo "Service failed to become healthy within the expected time."
exit 1
