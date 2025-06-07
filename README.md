# # SqlData-Migration

SqlData-Migration is a powerful and user-friendly tool designed for seamless online and offline data migration across various database systems, including RDBMS, NoSQL databases, and SQL script files. It supports migrating data between database connections, database files, or a combination of both. The tool aims to handle very large datasets efficiently and will provide a cross-platform experience for Windows, macOS, and Linux users.

Built with **Kotlin** and **Compose Multiplatform**.

## Key Features (Planned)

*   **Versatile Sources/Targets:**
    *   **Relational Databases (RDBMS):** Support for SQLite, PostgreSQL, MySQL, and more via JDBC.
    *   **SQL Script Files:** Use `.sql` files as a source or target for schema and data.
    *   **NoSQL Databases:** Support for MongoDB (initially), with potential for others.
    *   Flexible migration paths: DB-to-DB, File-to-DB, DB-to-File, File-to-File, RDBMS-to-NoSQL, etc.
*   **Configuration Management:**
    *   Auto-generation of configuration files.
    *   User-creatable and modifiable custom configuration files.
*   **Interactive Migration:**
    *   Drag-and-drop interface for selecting tables for migration.
    *   Visual column mapping.
    *   In-transit data modification capabilities (e.g., column renaming, value transformations).
*   **Performance:** Designed to handle very large datasets through efficient streaming and batching.
*   **Cross-Platform:** Native support for Windows, Linux, and macOS.

## Development Roadmap

For a detailed, step-by-step development plan, please see the [ROADMAP.md](ROADMAP.md) file. The roadmap outlines each development phase, tasks involved, and testing checkpoints.

## Tech Stack

*   **Primary Language:** Kotlin
*   **User Interface:** Compose Multiplatform for Desktop
*   **Build Tool:** Gradle
*   **Database Connectivity:** JDBC

## Contributing

Contributions are welcome! While the project is still in active development, we encourage you to:
- Review the [ROADMAP.md](ROADMAP.md) for planned features
- Open issues for bugs or feature requests
- Submit pull requests with improvements
- Share your feedback and ideas

Please ensure your code follows the project's style and includes appropriate tests.

## License

This project is currently under development. The license will be determined soon. All rights reserved until a license is chosen.