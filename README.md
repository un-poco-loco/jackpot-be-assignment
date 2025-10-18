# Jackpot Backend Service

Backend service for managing jackpot contributions and rewards in a betting system. The service processes bets through Kafka (or mock mode), manages jackpot pools with configurable contribution strategies, and evaluates bets for jackpot rewards.

## Features

- REST API for bet submission and jackpot evaluation
- **Swagger/OpenAPI documentation** for interactive API testing
- **Comprehensive input validation** with detailed error responses
- **Advanced error handling** with proper HTTP status codes and structured error messages
- Kafka integration for asynchronous bet processing (with mock mode support)
- Configurable contribution strategies (Fixed and Variable)
- Configurable reward strategies (Fixed and Variable)
- H2 in-memory database for data persistence
- Comprehensive unit and integration tests (89+ tests with 100% pass rate)

## Technology Stack

- **Java 17**
- **Spring Boot 3.2.1**
- **Spring Data JPA**
- **Spring Kafka**
- **H2 Database** (Development & Unit Tests)
- **PostgreSQL** (Testcontainers Integration Tests)
- **Maven**
- **Lombok**
- **Springdoc OpenAPI 2.3.0** (Swagger UI)
- **Testcontainers 1.19.3** (Integration Testing)

## Project Structure

```
src/main/java/com/jackpot/
├── controller/          # REST API controllers
│   ├── BetController.java
│   ├── JackpotController.java
│   └── GlobalExceptionHandler.java
├── dto/                # Data Transfer Objects
│   ├── BetRequest.java
│   └── JackpotEvaluationResponse.java
├── exception/          # Custom exceptions
│   ├── BetNotFoundException.java
│   ├── JackpotNotFoundException.java
│   └── InvalidBetIdException.java
├── service/            # Business logic layer
│   ├── BetService.java
│   └── JackpotService.java
├── kafka/              # Kafka producer and consumer
│   ├── KafkaProducer.java
│   └── KafkaConsumer.java
├── model/              # Domain models
│   ├── Bet.java
│   ├── Jackpot.java
│   ├── JackpotContribution.java
│   └── JackpotReward.java
├── repository/         # JPA repositories
│   ├── JackpotRepository.java
│   ├── ContributionRepository.java
│   └── RewardRepository.java
├── strategy/           # Strategy pattern implementations
│   ├── ContributionStrategy.java
│   ├── FixedContributionStrategy.java
│   ├── VariableContributionStrategy.java
│   ├── RewardStrategy.java
│   ├── FixedRewardStrategy.java
│   └── VariableRewardStrategy.java
└── JackpotApplication.java
```

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- **Docker** (Required for Testcontainers integration tests)
- (Optional) Apache Kafka if not using mock mode for production

## Installation and Setup

### 1. Clone the Repository

```bash
git clone <repository-url>
cd Jackpot
```

### 2. Build the Project

```bash
mvn clean install
```

### 3. Run the Application

#### Option A: Using Mock Mode (No Kafka Required)

```bash
mvn spring-boot:run
```

By default, the application runs in mock mode (`jackpot.mock-enabled=true` in `application.yml`). In this mode, bets are processed synchronously without requiring a Kafka broker.

#### Option B: Using Real Kafka

1. Start Kafka broker (ensure it's running on `localhost:9092`)

2. Update `application.yml`:
```yaml
jackpot:
  mock-enabled: false
```

3. Run the application:
```bash
mvn spring-boot:run
```

## API Documentation (Swagger UI)

Once the application is running, you can access the interactive API documentation at:

**Swagger UI:** `http://localhost:8080/swagger-ui.html`

The Swagger UI provides:
- Complete API documentation with request/response examples
- Interactive API testing interface
- Try-it-out functionality for all endpoints
- Schema definitions for all DTOs

**OpenAPI Specification:** `http://localhost:8080/v3/api-docs`

### Using Swagger UI for Testing

1. Navigate to `http://localhost:8080/swagger-ui.html`
2. Expand the endpoint you want to test
3. Click "Try it out"
4. Fill in the parameters
5. Click "Execute"
6. View the response

This is the easiest way to test the API without using curl or Postman!

## API Endpoints

### 1. Publish a Bet

**POST** `/api/bets`

Publishes a bet to the system for jackpot contribution processing.

**Request Body:**
```json
{
  "betId": 123,
  "userId": 456,
  "jackpotId": 1,
  "betAmount": 100.00
}
```

**Validation Rules:**
- `betId`: Required, must be positive
- `userId`: Required, must be positive
- `jackpotId`: Required, must be positive
- `betAmount`: Required, minimum 0.01

**Success Response:**
```
HTTP 202 Accepted
"Bet published successfully"
```

**Error Response (400 Bad Request):**
```json
{
  "error": "Validation failed",
  "status": 400,
  "timestamp": "2025-10-18T21:44:07.317",
  "details": {
    "betId": "Bet ID must be positive",
    "betAmount": "Bet amount must be at least 0.01"
  }
}
```

**Example:**
```bash
curl -X POST http://localhost:8080/api/bets \
  -H "Content-Type: application/json" \
  -d '{
    "betId": 123,
    "userId": 456,
    "jackpotId": 1,
    "betAmount": 100.00
  }'
```

### 2. Evaluate Jackpot Reward

**GET** `/api/jackpots/{betId}/evaluate`

Evaluates if a bet wins the jackpot and returns the reward amount.

**Success Response (200 OK):**
```json
{
  "won": true,
  "rewardAmount": 5000.00,
  "message": "Congratulations! You won the jackpot!"
}
```

**Error Response (404 Not Found):**
```json
{
  "error": "Bet not found",
  "status": 404,
  "timestamp": "2025-10-18T21:54:36.981",
  "message": "No contribution found for bet ID: 999999. The bet may not have been processed yet or does not exist."
}
```

**Error Response (400 Bad Request):**
```json
{
  "error": "Invalid bet ID",
  "status": 400,
  "timestamp": "2025-10-18T21:54:36.989",
  "message": "Invalid bet ID: -123. Bet ID must be a positive number."
}
```

**Example:**
```bash
curl http://localhost:8080/api/jackpots/123/evaluate
```

### 3. Get Jackpot Details

**GET** `/api/jackpots/{jackpotId}`

Retrieves details of a specific jackpot.

**Success Response (200 OK):**
```json
{
  "id": 1,
  "initialPoolValue": 1000.00,
  "currentPoolValue": 1200.00,
  "contributionType": "FIXED",
  "contributionConfig": "{\"percentage\": 0.10}",
  "rewardType": "FIXED",
  "rewardConfig": "{\"percentage\": 0.05}"
}
```

**Error Response (404 Not Found):**
```json
{
  "error": "Jackpot not found",
  "status": 404,
  "timestamp": "2025-10-18T21:54:36.936",
  "message": "Jackpot not found with ID: 999999"
}
```

**Example:**
```bash
curl http://localhost:8080/api/jackpots/1
```

## Configuration

### Application Properties

Key configuration properties in `src/main/resources/application.yml`:

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092  # Kafka broker address

  datasource:
    url: jdbc:h2:mem:jackpotdb         # H2 database URL

jackpot:
  kafka:
    topic: jackpot-bets                # Kafka topic name
  mock-enabled: true                   # Enable/disable mock mode
```

### Jackpot Strategies

The system comes with 3 pre-configured jackpots:

#### Jackpot 1: Fixed Contribution, Fixed Reward
- Initial pool: 1000.00
- Contribution: 10% of each bet
- Reward: 5% win chance

#### Jackpot 2: Variable Contribution, Variable Reward
- Initial pool: 5000.00
- Contribution: 15% decreasing as pool grows to 10000
- Reward: Increasing chance as pool grows (100% at 10000)

#### Jackpot 3: Fixed Contribution, Fixed Reward
- Initial pool: 2000.00
- Contribution: 5% of each bet
- Reward: 10% win chance

## Testing

The project includes comprehensive test coverage across all layers:

### Test Categories

#### 1. Unit Tests
- **Strategy Tests**: Tests for all contribution and reward strategies
  - `FixedContributionStrategyTest` - Fixed percentage contribution logic
  - `VariableContributionStrategyTest` - Variable contribution based on pool size
  - `FixedRewardStrategyTest` - Fixed probability reward evaluation
  - `VariableRewardStrategyTest` - Variable probability reward evaluation

- **Service Tests**: Business logic testing with mocked dependencies
  - `JackpotServiceTest` - Complete jackpot service functionality
  - `BetServiceTest` - Bet publishing and processing

#### 2. Integration Tests
- **Repository Tests**: JPA repository integration
  - `JackpotRepositoryIntegrationTest` - Database operations for jackpots
  - `ContributionRepositoryIntegrationTest` - Contribution CRUD operations
  - `RewardRepositoryIntegrationTest` - Reward record management

- **Controller Tests**: REST API endpoint testing
  - `BetControllerTest` - Bet submission endpoint
  - `JackpotControllerTest` - Jackpot evaluation and retrieval endpoints

#### 3. Validation and Error Handling Tests
- **Input Validation Tests**: Comprehensive validation testing
  - `BetControllerValidationTest` - 11 tests for input validation
    - Tests for negative values (betId, userId, jackpotId, betAmount)
    - Tests for null values and missing fields
    - Tests for bet amount below minimum (0.01)
    - Tests for multiple validation errors
    - Validates error response format and messages

- **Error Handling Tests**: Exception and error scenario testing
  - `JackpotControllerErrorHandlingTest` - 7 tests for error handling
    - Non-existent bet evaluation (404)
    - Invalid bet IDs: negative, zero, non-numeric (400)
    - Non-existent jackpot retrieval (404)
    - Invalid parameter types (400)
    - Validates proper HTTP status codes and error messages

#### 4. End-to-End Tests with H2
- `JackpotApplicationIntegrationTest` - Fast integration tests with H2
  - Full bet submission and processing flow
  - Multiple contributions and pool growth verification
  - Jackpot win evaluation and reset logic
  - Concurrent bet processing
  - Edge cases and error handling

#### 5. Testcontainers Integration Tests
- `JackpotApplicationTestcontainersIntegrationTest` - Real infrastructure testing
  - Tests with actual PostgreSQL and Kafka containers
  - Complete workflow with real message broker
  - Database transaction verification
  - Data persistence testing
  - Error handling with real infrastructure

- `KafkaIntegrationTest` - Dedicated Kafka testing
  - Kafka message publishing and consumption
  - Message ordering and processing
  - High-volume message handling
  - Error scenarios and recovery
  - Consumer group behavior

### Running Tests

#### Run All Tests
```bash
mvn test
```

#### Run Specific Test Class
```bash
mvn test -Dtest=FixedContributionStrategyTest
```

#### Run Integration Tests Only (H2-based, Fast)
```bash
mvn test -Dtest=*IntegrationTest -Dtest=!*TestcontainersIntegrationTest -Dtest=!KafkaIntegrationTest
```

#### Run Testcontainers Tests (Real Infrastructure, Requires Docker)
```bash
mvn test -Dtest=*TestcontainersIntegrationTest,KafkaIntegrationTest
```

#### Run Unit Tests Only
```bash
mvn test -Dtest=*Test -Dtest=!*IntegrationTest
```

#### Run Tests with Coverage Report
```bash
mvn clean test jacoco:report
```

#### Run All Tests (Including Testcontainers)
```bash
mvn clean verify
```

### Test Features

- **Testcontainers**: Real Kafka and PostgreSQL containers for integration tests
- **Mock Kafka**: H2-based tests run without requiring external services
- **In-Memory H2**: Fast unit and integration tests
- **PostgreSQL**: Real database testing via Testcontainers
- **Real Kafka**: Message broker testing with actual Kafka container
- **Comprehensive Assertions**: Validates all aspects of functionality
- **Edge Case Coverage**: Tests error conditions and boundary cases
- **Transaction Testing**: Verifies database transaction behavior
- **Concurrent Processing**: Tests multiple simultaneous operations
- **Container Reuse**: Testcontainers configured for reuse to speed up test execution

### Test Configuration

#### H2-based Tests (`application-test.yml`)
- Mock mode enabled by default
- In-memory H2 database
- Test-specific data fixtures
- Fast execution, no Docker required

#### Testcontainers Tests (`AbstractIntegrationTest`)
- Real PostgreSQL 15 container
- Real Kafka (Confluent Platform 7.5.0) container
- Dynamic configuration via `@DynamicPropertySource`
- Automatic container startup and cleanup
- Container reuse enabled for performance

### Testcontainers Setup

The project uses Testcontainers for realistic integration testing:

**Requirements:**
- Docker Desktop or Docker Engine running
- Internet connection (first run to download images)

**Containers Used:**
- `postgres:15-alpine` - PostgreSQL database
- `confluentinc/cp-kafka:7.5.0` - Apache Kafka

**Benefits:**
- Tests against real database and message broker
- No manual infrastructure setup
- Consistent test environment
- Automatic cleanup after tests

**First Run:**
```bash
# Ensure Docker is running
docker ps

# Run Testcontainers tests (will download images on first run)
mvn test -Dtest=*TestcontainersIntegrationTest
```

## Developer Tools

### Swagger UI
Access the interactive API documentation at: `http://localhost:8080/swagger-ui.html`

**Features:**
- Interactive API testing
- Complete request/response documentation
- Schema definitions
- Example values for all endpoints

### H2 Console

Access the H2 database console at: `http://localhost:8080/h2-console`

**Connection Details:**
- JDBC URL: `jdbc:h2:mem:jackpotdb`
- Username: `sa`
- Password: (leave empty)

## Database Schema

### Jackpot Table
Stores jackpot configurations and current pool values.

### Jackpot Contribution Table
Records all contributions made to jackpots from bets.

### Jackpot Reward Table
Records all jackpot rewards won by users.

## Design Patterns

### Strategy Pattern
The application uses the Strategy pattern for:
- **Contribution Strategies**: Different algorithms for calculating jackpot contributions
- **Reward Strategies**: Different algorithms for evaluating jackpot wins

This design allows easy extension with new strategies without modifying existing code.

### Error Handling

The application implements comprehensive error handling:
- **Custom Exceptions**: Domain-specific exceptions for clear error semantics
  - `BetNotFoundException` - When bet/contribution not found (404)
  - `JackpotNotFoundException` - When jackpot not found (404)
  - `InvalidBetIdException` - When bet ID is invalid (400)
- **Global Exception Handler**: `@RestControllerAdvice` for centralized error handling
- **Structured Error Responses**: Consistent JSON format with error, status, timestamp, message
- **Proper HTTP Status Codes**: 200, 202, 400, 404, 500

### Input Validation

All API inputs are validated using Bean Validation (Jakarta Validation):
- **Request Body Validation**: `@Valid` annotation on controller methods
- **Field-Level Validation**: `@NotNull`, `@Positive`, `@DecimalMin` on DTOs
- **Detailed Error Messages**: Field-specific validation errors in response
- **Type Validation**: Proper handling of type mismatches (string instead of number)

## Example Workflow

1. **Submit a bet:**
```bash
curl -X POST http://localhost:8080/api/bets \
  -H "Content-Type: application/json" \
  -d '{
    "betId": 100,
    "userId": 1,
    "jackpotId": 1,
    "betAmount": 50.00
  }'
```

2. **Check jackpot status:**
```bash
curl http://localhost:8080/api/jackpots/1
```

3. **Evaluate if bet won:**
```bash
curl http://localhost:8080/api/jackpots/100/evaluate
```

## Documentation

The project includes comprehensive documentation:

- **README.md** - This file, complete setup and API documentation
- **SWAGGER_TESTING_GUIDE.md** - Step-by-step Swagger UI testing guide with 5 complete scenarios
- **INPUT_VALIDATION.md** - Complete input validation reference with all validation rules and error examples
- **ERROR_HANDLING.md** - Comprehensive error handling documentation with all error scenarios and HTTP status codes
- **CLAUDE.md** - Project overview and technical requirements

## Troubleshooting

### Issue: Getting 400 Bad Request on bet submission
**Solution:** Check that all fields meet validation requirements:
- All IDs must be positive numbers
- Bet amount must be at least 0.01
- All required fields must be present

### Issue: Getting 404 Not Found when evaluating bet
**Solution:** The bet may not have been processed yet, or the bet ID doesn't exist. In mock mode, processing is immediate, so check that you're using the correct bet ID that was submitted.

### Issue: Application fails to start with Kafka errors
**Solution:** Set `jackpot.mock-enabled=true` in `application.yml` to run without Kafka.

### Issue: Tests failing
**Solution:** Ensure Java 17 is being used and run `mvn clean install`.

### Issue: H2 console not accessible
**Solution:** Check that `spring.h2.console.enabled=true` in `application.yml`.

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is developed for educational and demonstration purposes.

## Contact

For questions or support, please create an issue in the repository.
