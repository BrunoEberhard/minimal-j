# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Build
mvn clean install

# Run all tests
mvn test

# Run a single test class
mvn test -Dtest=ModelTest

# Run a single test method
mvn test -Dtest=ModelTest#testSpecificMethod

# Build without annotation processing (default in pom.xml)
mvn clean install -Dmaven.compiler.proc=none
```

Note: annotation processing is disabled by default (`-proc:none`). The new `GeneratePropertyConstants` annotation processor and `PropertyConstantsProcessor` are currently untracked — they may need to be wired in explicitly.

## Architecture

Minimal-J is a Java framework for building business applications. It enforces a strict layered architecture:

### Layers

**Model** (`org.minimalj.model`)
- Plain Java classes with public fields (not getters/setters)
- FieldUtils.isAllowedPrimitive specifies the allowed classes for model fields. Only object classes are allow not the java primitives like `int`.
- `String` fields require a `@Size` annotation — this is mandatory
- Implement `Rendering` to control how an object displays as text
- Implement `Code` for lookup/reference data (think enums that live in the DB)
- Embeddable classes (which have no id, for example `Money` or `Address`) can declared as public final. The fields of the embedded class are flattened in the database table.

**Frontend** (`org.minimalj.frontend`)
- `Frontend` is a singleton abstract class — implementations: `SwingFrontend` (desktop), `JsonFrontend` (web), `CheerpjFrontend`
- `Page` (interface): read-only content filling the display area; must be lightweight (do heavy work in `getContent()`)
- `Editor<T, RESULT>` (abstract): form-based editing dialog; subclass overrides `createForm()`, `save()`, and `load()`
- `Form<T>`: declares which fields are shown and in what layout — adds form elements by field reference using `Keys`
- Actions (`org.minimalj.frontend.action`): user-triggered operations; returned by `Application.getNavigation()` and `Page.getActions()`

**Backend** (`org.minimalj.backend`)
- `Backend` singleton executes `Transaction` instances
- Can run in-process or as a remote `SocketBackend` (configured via `MjBackendAddress`/`MjBackendPort` system properties)
- `Transaction<RETURN>` is a functional interface (`Serializable`) — business logic lives here, not in the frontend
- Transactions access the repository via `repository()` helper methods directly on the `Transaction` interface

**Repository** (`org.minimalj.repository`)
- `Repository` interface: `read`, `find`, `count`, `insert`, `update`, `delete`
- `SqlRepository`: ORM over H2 (embedded/test), MariaDB, PostgreSQL, or MSSQL
- Schema is derived automatically from entity classes registered in `Application.getEntityClasses()`
- `SubTable`/`ListTable`/`CrossTable` handle nested lists and many-to-many relations

**Application** (`org.minimalj.application`)
- Extend `Application` as entry point; override `getEntityClasses()`, `getNavigation()`, `createDefaultPage()`, optionally `createRepository()`, `createAuthentication()`
- `Configuration` reads system properties (prefixed `Mj*`) for runtime config
- Start via `WebServer.main(appClassName)` for web or `Swing.main(appClassName)` for desktop

### Key conventions

- Model fields are `public` — no getters/setters
- Each model class must have a static declaration of $ like `public static final Entity $ = Keys.of(Entity.class);` 
- Forms reference fields using the `Keys` mechanism (e.g., `form.line(Entity.$.field1, Entity.$.field2)`)
- A Form consists of lines of FormElement . For primitive fields the Form creates the FormElement . But applications also define special FormElement .  
- `Code` objects are always referenced as views — updating an entity does not cascade to its Code references
- Resources/i18n: property files named after the `Application` subclass, loaded via `MultiResourceBundle`
- `@Role` annotation on `Transaction` or `Page` for authorization; `Subject.hasRole(...)` for runtime checks
