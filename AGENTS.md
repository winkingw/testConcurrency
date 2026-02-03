# Repository Guidelines

## Project Structure & Module Organization
- Source code lives under `src/main/java/com/utgaming/testconcurrency` with packages for `controller`, `service`, `mapper`, `entity`, `config`, `util`, `common`, and `handle`.
- MyBatis mapper XML files are in `src/main/resources/mapper`.
- Configuration files are in `src/main/resources`, including `application.yml` and `application-prod.yml`.
- Tests are under `src/test/java/com/utgaming/testconcurrency`.

## Build, Test, and Development Commands
- `./mvnw.cmd clean package` (Windows) or `./mvnw clean package` (Unix): build a runnable Spring Boot jar.
- `./mvnw.cmd test`: run unit/integration tests via Maven Surefire.
- `./mvnw.cmd spring-boot:run`: run the app locally with default profile.
- `./mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=prod`: run with production profile.

## Coding Style & Naming Conventions
- Java 17 codebase using standard Spring Boot conventions.
- Use 4-space indentation and keep package names all lowercase.
- Classes are PascalCase (e.g., `ProductServiceImpl`); methods and fields are camelCase.
- Prefer constructor injection for Spring components and keep controller responses consistent (e.g., `Result` wrapper).
- Mapper interfaces live in `mapper` and align with XML names in `resources/mapper`.

## Testing Guidelines
- Testing uses `spring-boot-starter-test` (JUnit 5).
- Test classes should end with `Tests` (e.g., `ProductServiceTests`) and live in the matching package under `src/test/java`.
- Add tests for service logic and controller validation, especially for concurrency paths.

## Commit & Pull Request Guidelines
- This repository currently has no Git commits, so no established commit message convention exists.
- Suggested format: short, imperative summary (e.g., `Add Redis cache toggle`).
- PRs should include: a brief description, how to run or test changes, and any configuration updates (e.g., new `application-*.yml` keys).

## Configuration & Security Tips
- Redis and MySQL settings live in `application.yml`/`application-prod.yml`. Keep secrets out of source control when possible.
- When adding new config keys, document defaults and the active profile needed to use them.
