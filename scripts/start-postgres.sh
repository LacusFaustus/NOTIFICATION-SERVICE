#!/bin/bash

# Скрипт для запуска PostgreSQL в Docker
echo "Starting PostgreSQL in Docker..."

docker run --name notification-postgres \
  -e POSTGRES_DB=notifications \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=password \
  -p 5432:5432 \
  -d postgres:14-alpine

echo "PostgreSQL started on port 5432"
echo "Database: notifications"
echo "Username: postgres"
echo "Password: password"
