# Minimal-J Development Guidelines

This document provides essential information for developers working on the Minimal-J framework, a lightweight Java framework for building business applications.

## Build and Configuration Instructions

### Prerequisites

- Java 8 or higher
- Maven 3.8+ (or use the included Maven wrapper)
- MariaDB or H2 database for testing

### Building the Project

To build the project:

```bash
mvn clean install
```

## Project Structure

- `src/main/java` - Main source code
- `src/main/resources` - Resources (properties, XML, etc.)
- `src/test/java` - Test source code
- `src/test/resources` - Test resources
- `doc/` - Documentation
  - `user_guide/` - User guide
  - `topics.adoc` - Tutorial and examples
  - `release_notes.adoc` - Release notes
- `example/` - Example applications
- `ext/` - Extension modules

## Code Style Guidelines

### General Principles

- Keep it minimal: Avoid unnecessary complexity
- Follow Java conventions for naming and structure
- Maintain backward compatibility when possible
- Document public APIs

### Naming Conventions

- Classes: CamelCase (e.g., `BusinessEntity`)
- Methods and variables: camelCase (e.g., `getCustomer()`)
- Constants: UPPER_CASE with underscores (e.g., `MAX_CONNECTIONS`)
- Packages: lowercase with dots (e.g., `org.minimalj.frontend`)

### Code Organization

- Group related functionality in packages
- Keep classes focused on a single responsibility
- Limit class size and complexity
- Use interfaces to define contracts

## Testing Guidelines

### Testing Framework

The project uses JUnit for testing. Key annotations:

- `@Test` - Marks a method as a test
- `@BeforeEach` - Setup code to run before each test
- `@AfterEach` - Cleanup code to run after each test

## Framework Architecture

### Core Components

- **Model**: Simple POJOs with specific constraints
- **Backend**: Database access and business logic
- **Frontend**: UI rendering and user interaction
- **Application**: Configuration and bootstrapping

### Model Guidelines

- Models should be simple POJOs
- Use primitive types and String where possible
- Avoid complex object graphs
- Follow the constraints documented in the user guide

### Frontend Implementation

- UI is defined programmatically rather than declaratively
- Pages are composed of forms, tables, and other components
- Actions trigger business logic
- Multiple frontend implementations (Web, Desktop, Mobile) share the same backend

## Documentation

- Document public APIs with Javadoc
- Keep the user guide up-to-date with new features
- Include examples for complex features
- Update release notes for each release

## Contributing

- Fork the repository
- Create a feature branch
- Submit a pull request
- Ensure tests pass
- Follow the code style guidelines

## Release Process

- Update version in pom.xml
- Update release notes
- Build and test the project
- Deploy to Maven Central
- Tag the release in Git
- Update documentation

## Contact

For questions or support, contact:
- Bruno Eberhard, bruno.eberhard@pop.ch