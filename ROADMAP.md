# SqlData-Migration: Detailed Development Roadmap

This document outlines the phased development plan for the SqlData-Migration tool. Each phase includes specific tasks and concludes with a testing and roadmap update checkpoint.

**Core Technologies:** Kotlin, Compose Multiplatform (for UI), Gradle (for build management).

---

## Phase 0: Project Setup & Foundation

**Goal:** Establish the basic project structure, UI shell, and essential utilities.

*   [X] **Task 0.1:** Initialize Kotlin Gradle Project.
    *   Set up `build.gradle.kts` with Kotlin and necessary dependencies.
*   [X] **Task 0.2:** Integrate Compose Multiplatform for Desktop.
    *   Create a basic application window that launches on Windows, macOS, and Linux.
*   [X] **Task 0.3:** Setup Basic Logging Framework.
    *   Integrate a logging library (e.g., Logback, SLF4J) for diagnostics.
*   [X] **Task 0.4:** Define Core Data Structures.
    *   Initial Kotlin data classes for `DbConnectionConfig`, `TableSchema`, `ColumnSchema`, etc.
*   [X] **Checkpoint:** Test basic application launch on all target OS. Review and update roadmap.

---

## Phase 1: Core Engine - Connectivity & Basic Schema Reading (SQLite Focus)

**Goal:** Implement connectivity and schema reading for a single, simple database type (SQLite).

*   [X] **Task 1.1:** Design `DatabaseConnector` Interface.
    *   Define common methods: `connect()`, `disconnect()`, `getTables()`, `getSchema(tableName)`.
*   [X] **Task 1.2:** Implement `SQLiteConnector` (for SQLite database files).
    *   [X] Sub-task 1.2.1: Implement `connect()` using JDBC for SQLite.
    *   [X] Sub-task 1.2.2: Implement `getTables()` to list table names.
    *   [X] Sub-task 1.2.3: Implement `getSchema(tableName)` to read column names, data types, and basic constraints.
*   [X] **Task 1.3:** Basic UI for Source Selection.
    *   Compose UI element to allow users to select a source SQLite database file.
*   [X] **Checkpoint:** Test SQLite connectivity, schema reading, and UI file selection. Review and update roadmap.

---

## Phase 2: Core Engine - Configuration Management (Basic)

**Goal:** Enable saving and loading of migration configurations.

*   [X] **Task 2.1:** Define Configuration File Structure.
    *   Choose format (JSON or YAML recommended) and define structure for source/target connection details and basic table/column mappings.
*   [X] **Task 2.2:** Implement Configuration File Loading.
    *   Use a library like Jackson (for JSON) or SnakeYAML (for YAML) with Kotlin serialization.
*   [X] **Task 2.3:** Implement Configuration File Saving.
*   [X] **Task 2.4:** Basic UI for Configuration Management.
    *   Compose UI elements for loading and saving configuration files.
*   [X] **Checkpoint:** Test config file creation, loading, and saving. Review and update roadmap.

---

## Phase 3: Core Engine - Basic Data Migration (Single Table, SQLite to SQLite)

**Goal:** Implement the fundamental data migration pipeline for one table without complex transformations.

*   [X] **Task 3.1:** Implement Data Extraction (Streaming).
    *   Read data row-by-row (or in small batches) from a source SQLite table using the `SQLiteConnector`.
*   [X] **Task 3.2:** Implement Data Loading (Streaming).
    *   Write data row-by-row (or in small batches) to a target SQLite table (can be a new table or an existing one).
*   [X] **Task 3.3:** Basic UI for Migration Setup.
    *   Allow user to select one source table and specify a target table name for the SQLite-to-SQLite migration.
*   [X] **Task 3.4:** Trigger and Monitor Basic Migration.
    *   Implement logic to start the migration process and display basic progress/status.
*   [ ] **Checkpoint:** Test migration of a single table between two SQLite databases. Verify data integrity. Review and update roadmap.

---

## Phase 4: UI - Schema Display & Basic Manual Mapping

**Goal:** Provide visual feedback of database schemas and allow users to manually map columns.

*   [ ] **Task 4.1:** UI to Display Source Database Schema.
    *   Show list of tables from the connected source; on table selection, show its columns.
*   [ ] **Task 4.2:** UI to Display Target Database Schema (if applicable, e.g., connecting to existing target).
*   [ ] **Task 4.3:** Basic UI for Manual Column Mapping.
    *   For a selected source and target table, allow users to map source columns to target columns (e.g., using dropdowns or simple text inputs).
*   [ ] **Task 4.4:** Update Configuration with Mappings.
    *   Save these UI-defined mappings into the migration configuration file.
*   [ ] **Checkpoint:** Test schema display and column mapping UI. Verify mappings are saved correctly. Review and update roadmap.

---

## Phase 5: Core Engine - Expanding RDBMS Support (PostgreSQL & MySQL)

**Goal:** Add support for connection-based RDBMS like PostgreSQL and MySQL.

*   [ ] **Task 5.1:** Implement `PostgreSQLConnector`.
    *   [ ] Sub-task 5.1.1: Add PostgreSQL JDBC driver dependency.
    *   [ ] Sub-task 5.1.2: Implement `connect()` using JDBC for PostgreSQL (host, port, user, pass, db name).
    *   [ ] Sub-task 5.1.3: Adapt `getTables()` and `getSchema(tableName)` for PostgreSQL.
    *   [ ] Sub-task 5.1.4: Implement data extraction for PostgreSQL.
    *   [ ] Sub-task 5.1.5: Implement data loading for PostgreSQL.
*   [ ] **Task 5.2:** UI for PostgreSQL Connection Parameters.
    *   Add forms in the UI to input connection details for PostgreSQL.
*   [ ] **Task 5.3:** Implement `MySQLConnector`.
    *   [ ] Sub-task 5.3.1: Add MySQL JDBC driver dependency.
    *   [ ] Sub-task 5.3.2: Implement `connect()` using JDBC for MySQL (host, port, user, pass, db name).
    *   [ ] Sub-task 5.3.3: Adapt `getTables()` and `getSchema(tableName)` for MySQL.
    *   [ ] Sub-task 5.3.4: Implement data extraction for MySQL.
    *   [ ] Sub-task 5.3.5: Implement data loading for MySQL.
*   [ ] **Task 5.4:** UI for MySQL Connection Parameters.
    *   Add forms in the UI to input connection details for MySQL.
*   [ ] **Task 5.5:** (Optional, based on priority) Implement `SQLServerConnector`.
*   [ ] **Checkpoint:** Test connectivity, schema reading, and data migration with PostgreSQL and MySQL. Review and update roadmap.

---

## Phase 6: Core Engine - SQL Script File Support

**Goal:** Enable migration from and to `.sql` script files.

*   [ ] **Task 6.1:** Design `SqlScriptConnector` (or similar utility).
    *   Define methods for parsing SQL scripts (DDL for schema, DML for data).
    *   Define methods for generating SQL scripts from `TableSchema` and data.
*   [ ] **Task 6.2:** Implement SQL Script Parsing.
    *   [ ] Sub-task 6.2.1: Basic DDL parsing to extract `TableSchema` (e.g., `CREATE TABLE`).
    *   [ ] Sub-task 6.2.2: Basic DML parsing for data extraction (e.g., `INSERT INTO`).
    *   Consider using a SQL parsing library if complex scripts are to be supported.
*   [ ] **Task 6.3:** Implement SQL Script Generation.
    *   [ ] Sub-task 6.3.1: Generate DDL scripts from `TableSchema`.
    *   [ ] Sub-task 6.3.2: Generate DML scripts (inserts) from data.
*   [ ] **Task 6.4:** UI for SQL Script File Selection.
    *   Allow users to select source/target `.sql` files.
*   [ ] **Checkpoint:** Test parsing schema and data from `.sql` files, and generating `.sql` files. Review and update roadmap.

---

## Phase 7: Core Engine - Data Transformation (Basic)

**Goal:** Allow simple data transformations during migration.

*   [ ] **Task 7.1:** Define Transformation Rules in Configuration.
    *   Extend config file to include rules like column renaming, basic data type conversions (e.g., string to int if possible).
*   [ ] **Task 7.2:** Implement Transformation Logic in the Data Pipeline.
    *   Modify data as it streams from source to target based on configuration rules.
*   [ ] **Task 7.3:** UI to Define Simple Transformations.
    *   Allow users to specify transformations like renaming a target column or simple value mappings via the UI.
*   [ ] **Checkpoint:** Test migrations with basic transformations. Verify data correctness. Review and update roadmap.

---

## Phase 8: UI - Advanced Mapping & Drag-and-Drop

**Goal:** Enhance the UI for more intuitive table and column selection/mapping.

*   [ ] **Task 8.1:** Implement Drag-and-Drop for Table Selection.
    *   Allow users to drag tables from the source schema view to a "tables to migrate" list.
*   [ ] **Task 8.2:** Implement Drag-and-Drop for Column Mapping.
    *   Allow users to drag a source column to a target column for mapping (visual feedback).
*   [ ] **Task 8.3:** UI for Managing Multiple Table Migrations.
    *   Allow configuration of migrations for several tables within a single session/config file.
*   [ ] **Checkpoint:** Test new drag-and-drop UI features. Ensure usability and correct config updates. Review and update roadmap.

---

## Phase 9: Core Engine - Handling Large Data & Performance Optimization

**Goal:** Ensure the tool can handle large datasets efficiently.

*   [ ] **Task 9.1:** Optimize Streaming and Batching.
    *   Fine-tune data streaming mechanisms and batch sizes for inserts/updates across all supported connectors.
*   [ ] **Task 9.2:** Implement Asynchronous Migration Tasks.
    *   Run migration processes in background threads (using Kotlin Coroutines) to keep UI responsive.
    *   Implement robust progress reporting to the UI (e.g., rows processed, percentage complete, estimated time).
*   [ ] **Task 9.3:** Stress Testing.
    *   Test with significantly large datasets to identify and address bottlenecks.
*   [ ] **Checkpoint:** Verify performance improvements and UI responsiveness during large migrations. Review and update roadmap.

---

## Phase 10: Configuration - Auto-Generation & Advanced Editing

**Goal:** Improve configuration usability.

*   [ ] **Task 10.1:** Implement Auto-Generation of Basic Configuration.
    *   When source and target are connected, attempt to auto-map tables and columns with identical names and compatible types.
*   [ ] **Task 10.2:** UI for Advanced Configuration Editing.
    *   Provide a view (e.g., a text editor panel) for users to directly see and edit the raw JSON/YAML configuration if needed.
*   [ ] **Checkpoint:** Test auto-config generation and advanced editing features. Review and update roadmap.

---

## Phase 11: Features - In-Transit Row Modification (User-Defined)

**Goal:** Allow users to define rules for modifying row data as it's being migrated.

*   [ ] **Task 11.1:** Define Row Modification Rule Structure.
    *   Specify how users can define these rules in the configuration (e.g., simple find/replace on column values, conditional value changes - keep initial scope manageable).
*   [ ] **Task 11.2:** Implement Row Modification in Transformation Pipeline.
    *   Apply these rules to each row during the transformation step.
*   [ ] **Task 11.3:** UI for Defining Row Modification Rules.
    *   Provide an interface for users to add/edit these rules.
*   [ ] **Checkpoint:** Test migrations with user-defined row modifications. Verify correctness. Review and update roadmap.

---

## Phase 12: Cross-Platform Packaging & Testing

**Goal:** Prepare distributable versions of the application for all target platforms.

*   [ ] **Task 12.1:** Configure Gradle for Creating Distributables.
    *   Use Gradle plugins (e.g., `conveyor` or `jpackage` via Gradle tasks) to create `.exe`/`.msi` for Windows, `.dmg`/`.app` for macOS, and `.deb`/`.rpm`/AppImage for Linux.
*   [ ] **Task 12.2:** Thorough Testing on All Target Platforms.
    *   Install and run the packaged application on clean Windows, macOS, and Linux environments.
*   [ ] **Checkpoint:** Verify successful packaging and execution on all platforms. Review and update roadmap.

---

## Phase 13: Core Engine - NoSQL Database Support (MongoDB Focus)

**Goal:** Add support for a NoSQL database, starting with MongoDB.

*   [ ] **Task 13.1:** Research MongoDB Data Migration Strategies.
    *   Understand common patterns for migrating relational data to MongoDB (document modeling).
    *   Identify suitable MongoDB Kotlin drivers.
*   [ ] **Task 13.2:** Design `MongoDbConnector`.
    *   Define methods for `connect()`, `disconnect()`.
    *   Define methods for listing collections (analogous to tables).
    *   Define methods for inferring schema from sample documents (MongoDB is schema-less).
    *   Define methods for data extraction (reading documents).
    *   Define methods for data loading (writing documents).
*   [ ] **Task 13.3:** Implement `MongoDbConnector`.
    *   [ ] Sub-task 13.3.1: Add MongoDB driver dependency.
    *   [ ] Sub-task 13.3.2: Implement `connect()` and `disconnect()`.
    *   [ ] Sub-task 13.3.3: Implement collection listing and schema inference.
    *   [ ] Sub-task 13.3.4: Implement data extraction and loading.
*   [ ] **Task 13.4:** UI for MongoDB Connection Parameters.
    *   Add forms for MongoDB connection string / parameters.
*   [ ] **Task 13.5:** Basic Relational to MongoDB Mapping Strategy.
    *   Define a default strategy (e.g., each table becomes a collection, rows become documents).
    *   Consider UI for simple mapping adjustments.
*   [ ] **Checkpoint:** Test connectivity, schema inference, and basic data migration with MongoDB. Review and update roadmap.

---

## Phase 14: Documentation, Refinements & Release Preparation

**Goal:** Finalize the product for an initial release.

*   [ ] **Task 14.1:** Write User Manual & In-App Help.
    *   Create comprehensive documentation for end-users.
*   [ ] **Task 14.2:** Code Cleanup, Final Bug Fixing, and Performance Tweaks.
*   [ ] **Task 14.3:** Add Licensing Information.
*   [ ] **Checkpoint:** Final review of the application, documentation. Prepare for release. Update roadmap for v1.0 completion.

---
