# Maut Core Backend

A Spring Boot modular monolith application implementing a layered architecture with multiple functional modules. This service includes REST API endpoints and scheduled tasks, organized as a modular monolith for maintainability and scalability.

## Table of Contents

- [Modular Monolith Architecture](#modular-monolith-architecture)
- [Technologies](#technologies)
- [Project Structure](#project-structure)
- [Requirements](#requirements)
- [Setup](#setup)
- [Configuration](#configuration)
- [Running the Application](#running-the-application)
- [API Endpoints](#api-endpoints)
- [Testing](#testing)
- [Adding New Modules](#adding-new-modules)

## Modular Monolith Architecture

This application follows a modular monolith architecture, which strikes a balance between the simplicity of a monolith and the modularity of microservices. Key characteristics:

- **Single Deployment Unit**: The entire application is deployed as a single unit.
- **Module Isolation**: Business functionality is isolated into distinct modules.
- **Clear Boundaries**: Each module has well-defined boundaries and interfaces.
- **Shared Core Components**: Common functionality is shared across modules.

Benefits of this approach:
- Simplified development and deployment compared to microservices
- Clear separation of concerns
- Ability to extract modules into microservices later if needed
- Reduced operational complexity

## Technologies

- Java 17
- Spring Boot 2.7.x
- PostgreSQL (database)
- Flyway (database migrations)
- JUnit 5 & Mockito (testing)
- Maven (dependency management)

## Project Structure

The application follows a module-based architecture with a layered approach within each module:

```
maut-core-backend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/maut/core/
│   │   │       ├── common/             # Shared code used across modules
│   │   │       │   ├── config/         # Common configuration
│   │   │       │   ├── exception/      # Global exception handling
│   │   │       │   └── util/           # Utility classes
│   │   │       ├── modules/            # All feature modules go here
│   │   │       │   ├── hello/          # Hello module
│   │   │       │   │   ├── controller/ # Hello controllers
│   │   │       │   │   ├── dto/        # Hello DTOs
│   │   │       │   │   ├── model/      # Hello entities
│   │   │       │   │   ├── repository/ # Hello repositories
│   │   │       │   │   ├── scheduler/  # Hello schedulers
│   │   │       │   │   └── service/    # Hello services
│   │   │       │   └── [other-module]/ # Other modules with same structure
│   │   │       └── MautCoreApplication.java
│   │   └── resources/
│   │       ├── config/                 # Global configuration 
│   │       ├── db/
│   │       │   ├── migration/          # Global migrations
│   │       │   └── modules/            # Module-specific migrations
│   │       │       ├── hello/          # Hello module migrations
│   │       │       └── [other-module]/ 
│   │       └── application.properties
│   └── test/                           # Tests follow same structure
└── pom.xml
```

Each module follows the Controller-Service-Repository pattern:
- **Controllers**: Handle HTTP requests and responses
- **Services**: Contain business logic
- **Repositories**: Handle database operations
- **Models**: Entity definitions
- **DTOs**: Data Transfer Objects for API requests/responses
- **Schedulers**: Module-specific scheduled tasks

## Requirements

- JDK 17 or higher
- Maven 3.6.x or higher
- PostgreSQL 12 or higher

## Setup

1. **Clone the repository:**

```bash
git clone <repository-url>
cd maut-core-backend
```

2. **Set up PostgreSQL database:**

Create a PostgreSQL database named `maut_core`:

```bash
createdb maut_core
```

Alternatively, you can use the following SQL:

```sql
CREATE DATABASE maut_core;
```

3. **Database Configuration:**

Update database credentials in `src/main/resources/application.properties` if necessary:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/maut_core
spring.datasource.username=postgres
spring.datasource.password=postgres
```

## Configuration

The application uses a combination of properties files and JSON configuration:

### Application Properties

Standard Spring Boot configuration is in `src/main/resources/application.properties`.

### JSON Configuration

Custom application settings are in JSON format at `src/main/resources/config/application-config.json`:

```json
{
  "app": {
    "name": "maut-core-backend",
    "version": "1.0.0"
  },
  "features": {
    "helloMessage": {
      "cronSchedule": "0/10 * * * * ?",
      "deleteFrequency": 10
    }
  },
  "modules": {
    "hello": {
      "enabled": true,
      "description": "Hello message functionality"
    }
  }
}
```

These settings are loaded via a custom `JsonPropertySourceFactory` and can be accessed using Spring's `@Value` annotation or injected into configuration properties classes.

## Running the Application

### Using Maven

```bash
mvn spring-boot:run
```

### Using JAR file

```bash
mvn clean package
java -jar target/maut-core-backend-0.0.1-SNAPSHOT.jar
```

The application will start on port 8080 by default and automatically apply Flyway database migrations.

## API Endpoints

The service exposes the following REST endpoints, organized by module:

### Health Check

- **GET /v1/status** - Returns a 200 OK response if the service is running

### Hello Module

- **GET /v1/hello** - Retrieves the current hello message
- **POST /v1/hello** - Creates a new hello message
  - Request body: `{ "message": "Your message here" }`

## Testing

### Running Unit Tests

```bash
mvn test
```

### Running Integration Tests

```bash
mvn verify
```

## Adding New Modules

To add a new functional module to the application:

1. **Create the module structure**:
   ```
   com/maut/core/modules/[module-name]/
   ├── controller/
   ├── dto/
   ├── model/
   ├── repository/
   ├── service/
   └── scheduler/ (if needed)
   ```

2. **Add database migrations**:
   - Create a migration file in `db/modules/[module-name]/`
   - Use version numbers that follow the global versioning scheme
   - Add the migration location to `application.properties`:
     ```
     spring.flyway.locations=classpath:db/migration,classpath:db/modules/hello,classpath:db/modules/[module-name]
     ```

3. **Add module configuration**:
   - Update `application-config.json` to include the new module:
     ```json
     "modules": {
       "hello": { "enabled": true },
       "[module-name]": { 
         "enabled": true,
         "description": "Description of the new module"
       }
     }
     ```

4. **Implement the components**:
   - Create entity models
   - Implement repositories
   - Write service classes
   - Create DTOs
   - Implement controllers (follow API versioning conventions)
   - Add scheduler if needed

5. **Write tests** for all components following the existing patterns
