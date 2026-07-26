# EmployeeHub

A production-inspired microservices project built to gain hands-on experience with Apache Kafka, Event-Driven Architecture, the Transactional Outbox Pattern, and Debezium using Spring Boot.

The goal of this project is not to build another CRUD application, but to understand how modern distributed systems exchange data reliably without tight coupling.

---

## Project Goal

This repository documents my journey of learning and implementing enterprise messaging patterns used in modern backend systems.

Topics covered include:

- Event-Driven Architecture (EDA)
- Apache Kafka
- Transactional Outbox Pattern
- Debezium Change Data Capture (CDC)
- Kafka Connect
- Spring Boot Microservices
- Docker Compose
- PostgreSQL
- JWT Authentication & Authorization

---

## Architecture

```
                    ┌────────────────────┐
                    │    Auth Service    │
                    │ Spring Boot        │
                    └─────────┬──────────┘
                              │
                    Save User + Outbox Event
                              │
                              ▼
                     PostgreSQL Database
                              │
                    WAL (Write Ahead Log)
                              │
                              ▼
                         Debezium CDC
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

## Microservices

### Auth Service

Responsibilities

- User Registration
- User Login
- JWT Authentication
- Password Encryption
- Store Outbox Events
- Publish Domain Events using Debezium

---

### Notification Service

Responsibilities

- Consume Kafka Events
- Process User Registration Events
- Send Notifications (Upcoming)
- Email Integration (Upcoming)

---

## Infrastructure

Docker Compose provisions

- PostgreSQL
- Apache Kafka (KRaft Mode)
- Kafka Connect
- Debezium Connector

---

## Tech Stack

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

## Enterprise Concepts Implemented

- Microservices Architecture
- Event-Driven Communication
- Transactional Outbox Pattern
- Change Data Capture (CDC)
- Loose Coupling
- Asynchronous Messaging
- JWT Authentication
- Database Transactions
- Domain Events

---

## Why Transactional Outbox?

Instead of directly publishing Kafka messages from the application, events are first written into an Outbox table within the same database transaction.

Benefits:

- Prevents the Double Write Problem
- Guarantees data consistency
- Reliable event publishing
- Common enterprise architecture pattern

---

## Why Debezium?

Debezium continuously monitors PostgreSQL's Write Ahead Log (WAL).

Whenever a new Outbox record is inserted:

1. Debezium detects the database change.
2. Kafka Connect receives the change.
3. Kafka Connect publishes the event to Kafka.
4. Consumer microservices receive the event.

The application never needs to publish Kafka messages directly.

---

## Learning Roadmap

### Completed

- Spring Boot Authentication
- JWT Security
- PostgreSQL Integration
- Dockerized Infrastructure
- Apache Kafka Setup
- Kafka Connect
- Debezium
- Transactional Outbox Pattern

### In Progress

- Notification Service
- Kafka Consumers
- Email Notifications

### Planned

- Retry Mechanism
- Dead Letter Queue (DLQ)
- Idempotent Consumers
- Observability
- OpenTelemetry
- Resilience4j
- API Gateway
- Service Discovery
- Kubernetes Deployment

---

## Running the Project

```bash
docker compose up -d
```

Start services

```bash
auth-service
notification-service
```

---

## Repository Structure

```
EmployeeHub
│
├── auth-service
├── notification-service
├── infrastructure
└── docs
```

---

## Purpose

This repository is built as a learning project to understand enterprise backend architecture beyond CRUD applications.

The focus is on implementing production-inspired messaging patterns and gaining practical experience with technologies commonly used in distributed systems.

Contributions, suggestions, and feedback are always welcome.