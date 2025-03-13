# Instant Payment API

The **Instant Payment API** is a RESTful service built with **Spring Boot** that allows users to send money instantly
between accounts. It ensures **high availability**, **transactional integrity**, and **fault tolerance**. The service
integrates with **PostgreSQL** for data storage and **Kafka** for asynchronous notifications.

---

## Features

- **Instant Money Transfer**: Send money between accounts in real-time.
- **Balance Check**: Ensure sufficient balance before processing transactions.
- **Concurrency Handling**: Prevent double spending and ensure atomicity.
- **Kafka Integration**: Send transaction notifications asynchronously.
- **Fault Tolerance**: Retry mechanisms and error handling for robustness.
- **Containerization**: Ready for deployment using Docker.

---

## Technologies Used

- **Spring Boot**: Backend framework.
- **PostgreSQL**: Relational database for storing accounts and transactions.
- **Kafka**: Message broker for sending transaction notifications.
- **Docker**: Containerization for deployment.
- **Maven**: Build automation and dependency management.
- **Java 21**: Programming language.

---

## Database Schema

### Accounts Table

```sql
CREATE TABLE dbuser.ACCOUNT
(
    ID       VARCHAR(50) PRIMARY KEY,
    AMOUNT   DECIMAL(19, 4) NOT NULL,
    CURRENCY VARCHAR(10)    NOT NULL,
);
```

### Transaction Table

```sql
CREATE TABLE dbuser.TRANSACTION
(
    id                  SERIAL PRIMARY KEY,
    sender_account_id   VARCHAR(50)    NOT NULL,
    receiver_account_id VARCHAR(50)    NOT NULL,
    amount              DECIMAL(19, 4) NOT NULL,
    currency            VARCHAR(10)    NOT NULL,
    message            VARCHAR(100),
    status              VARCHAR(20)    NOT NULL,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

## Future Improvements

- **Security**: Adding security measurements to protect APIs like JWT or Oauth2
- **Database**: Further improve DB Tables
- **Kafka Communication**: Could add Kafka Consumer/Listener Service to the application for improved communication.
- **Payment Logic**: Further improve Payment Service
- **Tests**: Bigger test coverage for business logic and APIs. Add Integration tests as well.

---

## Setup and Installation

### Prerequisites

- Java 21 or higher
- Maven 3.x
- Docker and Docker Compose
- PostgreSQL
- Kafka

---

### Steps to Run the Application

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/BotondSiklosi/InstantPaymentAPI.git
   cd InstantPaymentAPI