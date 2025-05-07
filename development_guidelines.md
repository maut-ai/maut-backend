## Development Guidelines: LLM-Friendly Spring Boot & PostgreSQL Projects

These guidelines are intended to help maintain a simple, understandable, and maintainable codebase, especially when leveraging Large Language Models (LLMs) for code generation and assistance. Adhering to these practices will facilitate smoother collaboration with LLMs and ensure the longevity of the project. This project uses **Maven** as its build tool and **PostgreSQL** as its data store.

### I. Core Coding Principles

1.  **Prioritize Simplicity and Clarity:**
    * **Opt for Straightforward Solutions:** Avoid overly complex, clever, or obscure code. LLMs (and human developers) work best with code that is easy to read and reason about.
    * **Follow Common Idioms:** Stick to established Java and Spring Boot conventions.
    * **Code Style:** Adhere strictly to the **Google Java Style Guide**.

2.  **Emphasize Explicitness:**
    * **Use Clear and Descriptive Naming:**
        * Classes, methods, variables, and database elements (tables, columns) must have names that clearly indicate their purpose.
        * Avoid vague terms (`data`, `item`, `util`) or overly short abbreviations.
    * **Leverage Strong Typing:** Utilize Java's static typing fully. This provides clear contracts and reduces ambiguity for LLMs.
    * **Minimize "Magic":** While Spring Boot's auto-configuration is powerful, be explicit in configurations (e.g., bean definitions) when it enhances understanding, particularly in complex scenarios like security configurations or intricate third-party integrations.

3.  **Ensure Modularity and Single Responsibility:**
    * **Develop Small, Focused Components:** Structure the application into well-defined domain-specific modules. Each module should reside in its own package under `com.maut.core.modules.<domain_name>`. Within each domain module, components are further organized by type (e.g., `com.maut.core.modules.<domain_name>.model`, `com.maut.core.modules.<domain_name>.service`, `com.maut.core.modules.<domain_name>.controller`, `com.maut.core.modules.<domain_name>.dto`). This ensures clear separation of concerns and colocation of related logic.
    * **Adhere to the Single Responsibility Principle (SRP):** Each class and method should have one primary purpose. This makes it easier for LLMs to understand the scope and function of individual code units.

### II. Architectural and Design Standards

1.  **Implement a Layered Architecture (Controller-Service-Repository):**
    * This is the standard architecture for this project.
    * **Controllers (`@RestController`):**
        * Handle incoming HTTP requests and outgoing responses.
        * Delegate all business logic to Service components.
        * Keep controllers thin and focused on request/response handling.
        * *LLM Interaction:* Clearly define Request and Response Data Transfer Objects (DTOs).
    * **Services (`@Service`):**
        * Contain all business logic.
        * Orchestrate calls to Repositories and other Services.
        * *LLM Interaction:* Service methods must represent clear business operations. Document methods thoroughly with Javadoc (see Section IV.1).
    * **Repositories (`@Repository`):**
        * Manage all data access operations for our **PostgreSQL** database.
        * Typically, these will be interfaces extending Spring Data JPA's `JpaRepository`.
        * *LLM Interaction:* Use descriptive names for custom query methods. For complex queries, clearly state the intent if asking an LLM to generate it.

2.  **Utilize Data Transfer Objects (DTOs):**
    * Employ DTOs for all data transfer between layers (especially Controller-Service) and for API request/response payloads.
    * DTOs decouple the API contract from internal database entity structures.
    * (Decision pending on mandating a specific mapping library like MapStruct or allowing manual mapping under defined conditions).
    * *LLM Interaction:* Define DTOs as simple POJOs with clear field names. LLMs can assist with generating mapping code between DTOs and Entities.

3.  **Design RESTful APIs:**
    * Strictly follow REST principles: standard HTTP methods (GET, POST, PUT, DELETE, etc.), clear resource-based URLs, and stateless communication.
    * **API Versioning:** API versioning **must** be implemented via URI path (e.g., `/api/v1/resource`, `/api/v2/resource`).
    * Document APIs using OpenAPI/Swagger. The specification file should be kept up-to-date.
    * (Decisions pending on standard API error response structure, endpoint naming conventions, and pagination parameter standards).
    * *LLM Interaction:* Provide the OpenAPI specification or detailed descriptions of endpoints when requesting LLM assistance for API-related code.

4.  **Apply Dependency Injection (DI):**
    * Use Spring's `@Autowired` for DI.
    * **Prefer Constructor Injection** for mandatory dependencies to ensure immutability and make dependencies explicit.
    * (Decision pending on explicitly permitted exceptions for field or setter injection).

5.  **Manage Configuration Externally:**
    * Centralize all application configuration in `application.properties` or `application.yml`. (Decision pending on preferring one format or allowing JSON under specific conditions).
    * Use meaningful and hierarchical property keys.
    * *LLM Interaction:* Provide specific requirements when asking LLMs to generate configuration snippets.

### III. Database Interaction (PostgreSQL & Spring Data JPA)

1.  **Define Clear JPA Entities (`@Entity`):**
    * Entities must accurately map to **PostgreSQL** tables.
    * **Primary Keys:** All primary key fields (typically named `id`) **must** use `java.util.UUID` as their type. Corresponding database columns should be of type `UUID`.
    * Use appropriate JPA annotations for relationships (`@OneToMany`, `@ManyToOne`, `@ManyToMany`), column specifics (`@Column`), and primary key generation (`@Id`, `@GeneratedValue`). For UUIDs, `@GeneratedValue(generator = "UUID")` and `@GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")` (from Hibernate) or similar standard JPA mechanisms should be used.
    * (Decisions pending on default fetch types, cascade types, and guidelines for lazy loading large fields).
    * *LLM Interaction:* Provide database schema (DDL) or clear descriptions of tables and relationships when requesting entity generation.

2.  **Leverage Spring Data JPA Repositories:**
    * Use `JpaRepository` for standard CRUD operations to reduce boilerplate.
    * For custom queries:
        * First, attempt to use derived query methods.
        * If more complex, use `@Query` with JPQL. Native SQL should be a last resort and clearly justified (decision pending on specific circumstances for native SQL and performance guidelines for derived queries).
    * *LLM Interaction:* For complex query generation, clearly articulate the required logic in natural language. Always review and test LLM-generated queries.

3.  **Implement Database Migrations with Flyway:**
    * **Flyway must be used** for managing and versioning all schema changes for the PostgreSQL database.
    * Migration scripts must be clear and incremental.
    * (Decisions pending on naming conventions for migration scripts and rules for changes within a single script).
    * *LLM Interaction:* LLMs can help draft Flyway migration scripts if you precisely describe the required schema modifications.

### IV. LLM-Specific Collaboration Practices (and General Best Practices)

1.  **Write Comprehensive Javadoc and Code Comments:**
    * **Class-Level Javadoc:** Explain the overall responsibility of each class.
    * **Method-Level Javadoc:** All **public** methods **must** have Javadoc. This Javadoc **must** include `@param` for all parameters, `@return` if the method is non-void, and `@throws` for any checked exceptions declared or documented unchecked exceptions that are part of the method's contract.
    * **Inline Comments:** Use inline comments **judiciously**. They should only be used to explain complex or non-obvious blocks of logic. Avoid commenting self-evident code or every single line.
    * **`@author` tags:** (Decision pending on usage).
    * *LLM Benefit:* Rich comments and Javadoc provide essential context for LLMs, enabling them to understand existing code and generate more accurate and relevant new code.

2.  **Formulate Clear and Atomic Prompts for LLMs:**
    * When requesting code from an LLM, be highly specific.
    * Break down complex tasks into smaller, well-defined sub-tasks for the LLM.

3.  **Provide Sufficient Context in Prompts:**
    * Include relevant existing code snippets.
    * Specify versions of Spring Boot, Java, PostgreSQL, and other key libraries if relevant to the request.

4.  **Engage in Iterative Refinement with LLMs:**
    * Treat LLM-generated code as a first draft.
    * Always review, test thoroughly, and refactor LLM output.
    * If the output isn't correct, provide specific feedback to the LLM and ask for revisions.

5.  **Maintain Standard Project Structure:**
    * Adhere to the standard **Maven** project directory layout (e.g., `src/main/java`, `src/main/resources`, `src/test/java`).
    * **Project-Specific Sub-Package Structure:** All core business logic is organized into domain-specific modules under `com.maut.core.modules`. Each module, representing a distinct business domain (e.g., `session`, `user`, `clientapplication`), should contain its own sub-packages for `model`, `repository`, `service`, `controller`, `dto`, `security`, etc., as applicable. For example: `com.maut.core.modules.user.model`, `com.maut.core.modules.session.controller`.
     * Common utilities or configurations that are not domain-specific and are shared across multiple modules may reside in appropriate packages under `com.maut.core.common` or `com.maut.core.config` respectively, but domain-specific logic must remain within its module.

6.  **Implement Robust Error Handling:**
    * Develop a consistent error handling strategy.
    * Use custom, specific exception classes where appropriate.
    * Employ `@ControllerAdvice` for global exception handling to provide standardized API error responses.
    * (Decisions pending on a defined hierarchy of custom base exceptions and standardized internal error codes).
    * *LLM Interaction:* You can ask LLMs to incorporate try-catch blocks or suggest appropriate exception types for specific operations.

7.  **Prioritize Testing:**
    * Write comprehensive unit tests (JUnit, Mockito) for services and other logic-heavy components.
    * Write integration tests (`@SpringBootTest`) for API endpoints and interactions between layers.
    * (Decisions pending on minimum code coverage, mandatory unit test components, integration test guidelines, and TDD adoption).
    * *LLM Interaction:* LLMs can assist in generating test boilerplate or suggesting test cases if provided with clear functional descriptions of the code under test.

By adhering to these guidelines, we aim to build a high-quality, maintainable Spring Boot application that effectively leverages the capabilities of LLMs throughout the development lifecycle.
