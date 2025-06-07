package com.sqldatamigration.core.connectors

import com.sqldatamigration.core.models.DbConnectionConfig
import com.sqldatamigration.core.models.TableSchema

/**
 * Defines the contract for database connectors.
 * Implementations of this interface will provide mechanisms to connect to,
 * introspect, and interact with specific database systems.
 */
interface DatabaseConnector {

    /**
     * Establishes a connection to the database using the provided configuration.
     * This function should be called before any other operations.
     *
     * @param config The database connection configuration.
     * @throws Exception if the connection fails.
     */
    suspend fun connect(config: DbConnectionConfig)

    /**
     * Closes the active database connection.
     * It's important to call this to release database resources.
     */
    suspend fun disconnect()

    /**
     * Retrieves a list of table names from the connected database.
     *
     * @return A list of table names.
     * @throws IllegalStateException if not connected.
     * @throws Exception for other database errors.
     */
    suspend fun getTables(): List<String>

    /**
     * Retrieves the schema for a specific table.
     *
     * @param tableName The name of the table to get the schema for.
     * @return A [TableSchema] object representing the table's structure, or null if the table doesn't exist.
     * @throws IllegalStateException if not connected.
     * @throws Exception for other database errors.
     */
    suspend fun getSchema(tableName: String): TableSchema?
}
