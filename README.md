# EmployeeHub

A production-inspired Spring Boot microservices project built to gain hands-on experience with Apache Kafka and modern event-driven architecture patterns used in enterprise applications.

The primary objective of this project is to understand how distributed systems communicate reliably using Apache Kafka, the Transactional Outbox Pattern, Debezium Change Data Capture (CDC), and asynchronous messaging while following clean microservices architecture principles.

---

## Architecture

```text
                    ┌────────────────────┐
                    │    Auth Service    │
                    │   Spring Boot      │
                    └─────────┬──────────┘
                              │
                  Save User + Outbox Event
                              │
                              ▼
                        PostgreSQL
                              │
                     Write Ahead Log (WAL)
                              │
                              ▼
                      Debezium Connector
                              │
                              ▼
                        Kafka Connect
                              │
                              ▼
                         Apache Kafka
                              │
                              ▼
                  Notification Service
```

---

## Project Goals

This repository is focused on understanding and implementing production-grade backend concepts rather than building a traditional CRUD application.

Key learning objectives include:

- Event-Driven Architecture
- Apache Kafka
- Transactional Outbox Pattern
- Debezium Change Data Capture (CDC)
- Kafka Connect
- Spring Boot Microservices
- JWT Authentication
- Dockerized Development Environment
- Reliable Asynchronous Communication

---

## Services

### Auth Service

Responsible for

- User Registration
- User Authentication
- JWT Token Generation
- Password Encryption
- Transactional Outbox Pattern
- Publishing Domain Events through Debezium

---

### Notification Service

Responsible for

- Consuming Kafka Events
- Processing User Registration Events
- Notification Workflow

> Email integration and additional notification channels will be implemented in upcoming iterations.

---

## Infrastructure

The project uses Docker Compose to provision the complete local development environment.

- PostgreSQL
- Apache Kafka (KRaft Mode)
- Kafka Connect
- Debezium PostgreSQL Connector

---

## Technology Stack

### Backend

- Java 21
- Spring Boot
- Spring Security
- Spring Data JPA
- Spring Kafka
- JWT
- Lombok

### Messaging

- Apache Kafka
- Kafka Connect
- Debezium

### Database

- PostgreSQL

### DevOps

- Docker
- Docker Compose

---

## Enterprise Concepts Demonstrated

- Microservices Architecture
- Event-Driven Architecture
- Transactional Outbox Pattern
- Change Data Capture (CDC)
- Domain Events
- Loose Coupling
- Asynchronous Messaging
- JWT Authentication
- Database Transactions

---

## Why Transactional Outbox?

Instead of publishing Kafka messages directly from the application, business events are first stored in an Outbox table within the same database transaction.

This approach helps eliminate the Double Write Problem by ensuring both the business data and the event are committed atomically.

---

## Why Debezium?

Debezium continuously monitors PostgreSQL's Write Ahead Log (WAL).

Whenever a new Outbox record is committed:

1. Debezium detects the database change.
2. Kafka Connect captures the event.
3. Kafka Connect publishes the event to Apache Kafka.
4. Consumer microservices receive the event asynchronously.

This allows services to publish domain events without directly interacting with Kafka producers.

---

## Current Progress

### Completed

- Spring Boot Authentication
- JWT Security
- PostgreSQL Integration
- Dockerized Infrastructure
- Apache Kafka Setup
- Kafka Connect
- Debezium CDC
- Transactional Outbox Pattern

### In Progress

- Notification Service
- Kafka Consumers
- Email Notifications

### Planned

- Dead Letter Queue (DLQ)
- Retry Mechanism
- Idempotent Consumers
- API Gateway
- Service Discovery
- Resilience4j
- OpenTelemetry
- Kubernetes Deployment

---

## Repository Structure

```text
EmployeeHub
│
├── auth-service
├── notification-service
├── infrastructure
└── docs
```

---

## Running the Project

Start the infrastructure:

```bash
docker compose up -d
```

Run the microservices:

```text
auth-service
notification-service
```

---

## Purpose

This repository serves as a hands-on learning project for exploring enterprise backend architecture and messaging systems using Spring Boot and Apache Kafka.

The implementation emphasizes reliability, scalability, and production-inspired design patterns commonly used in modern distributed systems.

---

## Copyright

© 2026 Gurinder Singh. All rights reserved.

This repository is provided for portfolio and educational purposes.

Unauthorized redistribution, commercial use, or reproduction of this project without permission is prohibited.