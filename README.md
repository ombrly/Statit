# Statit

A platform for submitting, ranking, and analyzing personal metrics across user-defined categories with real-time statistical processing. 

## Getting Started

### Prerequisites
- Java 25
- PostgreSQL 15+
- Gradle

### Database Setup
```sql
CREATE DATABASE statit_db;
CREATE USER ranking_admin WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE statit_db TO ranking_admin;
```

### Run Locally
```bash
git clone https://github.com/Charblez/Global-Ranking-System.git
cd Global-Ranking-System/backend
./gradlew bootRun
```

The API will be available at `http://localhost:8080`

### Run Tests
```bash
./gradlew test
```

## API Endpoints
| Method | Endpoint  | Description |
|---|---|---|

## Architecture
```
com.statit.backend
├── config/         # Spring configuration
├── controller/     # REST API endpoints
├── dto/            # Request/response objects
├── exception/      # Error handling
├── model/          # JPA entities
├── repository/     # Database queries
├── service/        # Business logic
```

## Tech Stack
- **Backend:** Java 25, Spring Boot 4.0, Hibernate
- **Database:** PostgreSql 15 with JSONB for dynamic filtering

## Features

## Contributing
1. Create a branch: `user/<name>/backend/<feature>`
2. PR into 'dev' for code review
3. PRs into 'main' require passing tests

## Team
- Charles Bassani
- Wilson Jimenez
- Kenneth Chan
- Derek Ly