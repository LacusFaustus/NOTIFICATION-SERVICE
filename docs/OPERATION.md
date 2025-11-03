# Operations Guide

## Health Monitoring

### Health Check Endpoints
```bash
# Overall application health
curl http://localhost:8080/actuator/health

# Liveness probe (is application running?)
curl http://localhost:8080/actuator/health/liveness

# Readiness probe (can accept traffic?)
curl http://localhost:8080/actuator/health/readiness

# Custom health check with details
curl http://localhost:8080/actuator/health/readiness?includeComponents=db,redis,rabbitmq
