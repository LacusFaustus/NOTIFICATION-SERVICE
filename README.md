# Notification Service
Java Spring Boot PostgreSQL Redis RabbitMQ Coverage Tests License Version

## 🎯 Статус проекта
✅ Все тесты пройдены (144 теста)  
✅ Покрытие кода 80%+  
✅ Производственная готовность  
✅ Полная документация  
✅ Docker & Kubernetes поддержка  
✅ CI/CD конфигурация  
![Java CI](https://github.com/LacusFaustus/NOTIFICATION-SERVICE/actions/workflows/ci.yml/badge.svg)
![Coverage](https://codecov.io/gh/LacusFaustus/NOTIFICATION-SERVICE/branch/main/graph/badge.svg)

A high-performance microservice for handling email and push notifications...

## Features
📧 **Email Notifications**: Support for HTML and text emails with attachments  
📱 **Push Notifications**: Support for iOS, Android, and web push notifications  
🚀 **Async Processing**: RabbitMQ-based message queue for reliable processing  
🔄 **Retry Mechanism**: Automatic retry with exponential backoff for failed notifications  
💾 **Template Management**: Dynamic email templates with variable substitution  
📊 **Monitoring**: Comprehensive metrics and health checks  
🔒 **Security**: JWT authentication and RBAC authorization  
🐳 **Containerized**: Docker and Kubernetes support  
📈 **Scalable**: Horizontal scaling support with load balancing

## Quick Start
### Prerequisites
- Java 17+
- Docker and Docker Compose
- Maven 3.6+

### Running with Docker Compose
1. Clone the repository
```bash
git clone https://github.com/LacusFaustus/NOTIFICATION-SERVICE.git
cd notification-service
