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
- FieldUtils.isAllowedPrimitive specifies the allowed classes for model fields. Besides the object classes the primitives `int` and `long` are allowed (they act as not-null / NOT NULL fields). A primitive `boolean` field needs a companion method returning `Keys.fieldOf(this, "fieldName")`; other primitives are not allowed.
- `String` fields require a `@Size` annotation — this is mandatory
- Fields annotated with `@NotEmpty` become `NOT NULL` columns. Tests that insert entities must fill these fields, otherwise the insert fails
- Model class names must be unique across all packages (table names are derived from the simple class name). Only enums may share a name. Checked by `ModelTest`
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
- Optional initial data for `Code` classes: if a resource `<SimpleClassName>.csv` exists next to the class (e.g. `PetType.csv`), it is read on every start, after schema preparation (skipped only with `schemaPreparation=none`). The first line is the header with field names, separator is `,`. Code fields in the csv are given by their id. Only missing rows are inserted: rows with an `id` column are matched by id, rows without by comparing all fields. Existing rows are never updated or deleted. `static final` constants of a `Code` class (fields of its own type, except `$`) are inserted the same way

**Lists in entities**

A `List` field is saved together with its owning entity (insert / update / delete); there is no separate save for list elements. Behaviour depends on the element class — details in `doc/lists.adoc`:
- no `id`: owned by the parent, stored in table `PARENT__FIELD`, loaded eagerly. These elements cannot be referenced from other entities — if that is needed, give the element class an `id` (making it an entity of its own)
- `id`: cross table, lazy `RelationList`. New elements (without id yet) are inserted with the parent, but updating the parent does **not** save changes on already existing elements
- `id` and implements `Dependable<PARENT>`: the element table references the parent; elements are updated, inserted and deleted with the parent

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
