package com.sqldatamigration.core.models

import kotlinx.serialization.Serializable

/**
 * Represents the configuration for connecting to a database.
 * This is a sealed interface to allow for different types of connection parameters.
 */
@Serializable
sealed interface DbConnectionConfig {
    @Serializable
    data class SQLiteConfig(
        val filePath: String
    ) : DbConnectionConfig

    @Serializable
    data class JdbcConnectionConfig(
        val jdbcUrl: String,
        val user: String? = null,
        val password: String? = null,
        val driverClassName: String // e.g., "org.postgresql.Driver"
    ) : DbConnectionConfig
    // Add other specific configs like MySQLConfig, PostgreSQLConfig if more tailored fields are needed later
}

/**
 * Represents the schema of a database table.
 *
 * @property name The name of the table.
 * @property columns A list of [ColumnSchema] objects representing the columns in this table.
 */
data class TableSchema(
    val name: String,
    val columns: List<ColumnSchema>
)

/**
 * Represents the schema of a column within a database table.
 *
 * @property name The name of the column.
 * @property dataType The SQL data type of the column (e.g., "VARCHAR(255)", "INT", "BOOLEAN").
 * @property isNullable True if the column can store NULL values, false otherwise.
 * @property isPrimaryKey True if this column is part of the primary key, false otherwise.
 * @property ordinalPosition The 1-based index of the column in the table definition.
 */
data class ColumnSchema(
    val name: String,
    val dataType: String, // We can refine this later with a more structured type if needed
    val isNullable: Boolean = true,
    val isPrimaryKey: Boolean = false,
    val ordinalPosition: Int = 0 // Or some other default if not applicable
    // Potentially add: defaultValue, characterMaximumLength, numericPrecision, numericScale, etc.
)

// Example usage (can be removed later, just for illustration):
fun main() {
    val sqliteConfig = DbConnectionConfig.SQLiteConfig("/path/to/my.db")
    val postgresConfig = DbConnectionConfig.JdbcConnectionConfig(
        jdbcUrl = "jdbc:postgresql://localhost:5432/mydatabase",
        user = "admin",
        password = "secret",
        driverClassName = "org.postgresql.Driver"
    )

    val idColumn = ColumnSchema(name = "id", dataType = "INTEGER", isNullable = false, isPrimaryKey = true, ordinalPosition = 1)
    val nameColumn = ColumnSchema(name = "name", dataType = "VARCHAR(100)", isNullable = false, ordinalPosition = 2)
    val emailColumn = ColumnSchema(name = "email", dataType = "VARCHAR(255)", ordinalPosition = 3)

    val usersTable = TableSchema(
        name = "users",
        columns = listOf(idColumn, nameColumn, emailColumn)
    )

    println(sqliteConfig)
    println(postgresConfig)
    println(usersTable)
}
