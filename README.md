# Notification Service

![Java CI](https://github.com/LacusFaustus/NOTIFICATION-SERVICE/actions/workflows/ci.yml/badge.svg)
![Coverage](https://img.shields.io/badge/coverage-85%25-brightgreen)

A high-performance microservice for handling email and push notifications...

## Features

- 📧 **Email Notifications**: Support for HTML and text emails with attachments
- 📱 **Push Notifications**: Support for iOS, Android, and web push notifications
- 🚀 **Async Processing**: RabbitMQ-based message queue for reliable processing
- 🔄 **Retry Mechanism**: Automatic retry with exponential backoff for failed notifications
- 💾 **Template Management**: Dynamic email templates with variable substitution
- 📊 **Monitoring**: Comprehensive metrics and health checks
- 🔒 **Security**: JWT authentication and RBAC authorization
- 🐳 **Containerized**: Docker and Kubernetes support
- 📈 **Scalable**: Horizontal scaling support with load balancing

## Quick Start

### Prerequisites
- Java 17+
- Docker and Docker Compose
- Maven 3.6+

### Running with Docker Compose

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd notification-service
