package com.sqldatamigration.core.connectors

import com.sqldatamigration.core.models.DbConnectionConfig
import com.sqldatamigration.core.models.TableSchema
import org.slf4j.LoggerFactory
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

class SQLiteConnector : DatabaseConnector {

    private var connection: Connection? = null
    private val logger = LoggerFactory.getLogger(SQLiteConnector::class.java)

    override suspend fun connect(config: DbConnectionConfig) {
        if (config !is DbConnectionConfig.SQLiteConfig) {
            val errorMsg = "Invalid configuration type for SQLiteConnector. Expected SQLiteConfig."
            logger.error(errorMsg)
            throw IllegalArgumentException(errorMsg)
        }

        try {
            // The SQLite JDBC driver is usually registered automatically.
            // Class.forName("org.sqlite.JDBC") // Not strictly necessary for modern JDBC drivers
            logger.info("Attempting to connect to SQLite database: ${config.filePath}")
            connection = DriverManager.getConnection("jdbc:sqlite:${config.filePath}")
            connection?.autoCommit = false // Recommended for transactional control, though might change later
            logger.info("Successfully connected to SQLite database: ${config.filePath}")
        } catch (e: SQLException) {
            logger.error("Failed to connect to SQLite database: ${config.filePath}", e)
            throw e // Re-throw to allow higher-level handling
        } catch (e: ClassNotFoundException) {
            logger.error("SQLite JDBC driver not found.", e)
            throw RuntimeException("SQLite JDBC driver not found. Ensure it's in the classpath.", e)
        }
    }

    override suspend fun disconnect() {
        try {
            connection?.let {
                if (!it.isClosed) {
                    it.close()
                    logger.info("Successfully disconnected from SQLite database.")
                }
            }
        } catch (e: SQLException) {
            logger.error("Error while disconnecting from SQLite database.", e)
            // Decide if to re-throw or just log
        } finally {
            connection = null
        }
    }

    override suspend fun getTables(): List<String> {
        if (connection == null || connection!!.isClosed) {
            throw IllegalStateException("Not connected to a database. Call connect() first.")
        }
        val tables = mutableListOf<String>()
        try {
            val metaData = connection!!.metaData
            // For SQLite, catalog and schemaPattern can be null. We are interested in tables.
            val resultSet = metaData.getTables(null, null, "%", arrayOf("TABLE"))
            while (resultSet.next()) {
                val tableName = resultSet.getString("TABLE_NAME")
                // SQLite often includes system tables like 'sqlite_sequence', 'sqlite_master', etc.
                // We might want to filter these out, or tables starting with 'sqlite_'.
                if (!tableName.startsWith("sqlite_")) {
                    tables.add(tableName)
                }
            }
            resultSet.close()
            logger.info("Retrieved tables: $tables")
            return tables
        } catch (e: SQLException) {
            logger.error("Error retrieving tables from SQLite database.", e)
            throw e // Re-throw to allow higher-level handling
        }
    }

    override suspend fun getSchema(tableName: String): TableSchema? {
        if (connection == null || connection!!.isClosed) {
            throw IllegalStateException("Not connected to a database. Call connect() first.")
        }
        try {
            val metaData = connection!!.metaData

            // First, check if the table exists by trying to get its columns
            // If getColumns returns an empty ResultSet for a non-existent table, it's tricky.
            // A more robust way is to check if getTables() includes it, but that's an extra call.
            // For now, we'll assume if getColumns is empty, the table might not exist or has no columns.
            // A better check: query sqlite_master for the table name.
            var tableExists = false
            metaData.getTables(null, null, tableName, arrayOf("TABLE")).use { rs ->
                if (rs.next()) {
                    tableExists = true
                }
            }
            if (!tableExists) {
                logger.warn("Table '$tableName' not found.")
                return null
            }

            // Get primary key columns for the table
            val primaryKeyColumns = mutableSetOf<String>()
            metaData.getPrimaryKeys(null, null, tableName).use { pkResultSet ->
                while (pkResultSet.next()) {
                    primaryKeyColumns.add(pkResultSet.getString("COLUMN_NAME"))
                }
            }

            val columns = mutableListOf<com.sqldatamigration.core.models.ColumnSchema>()
            // Parameters for getColumns: catalog, schemaPattern, tableNamePattern, columnNamePattern
            val columnsResultSet = metaData.getColumns(null, null, tableName, null)
            while (columnsResultSet.next()) {
                val columnName = columnsResultSet.getString("COLUMN_NAME")
                val dataType = columnsResultSet.getString("TYPE_NAME")
                val isNullable = columnsResultSet.getString("IS_NULLABLE") == "YES"
                val ordinalPosition = columnsResultSet.getInt("ORDINAL_POSITION")
                // Remarks might contain default value or other info, but it's driver-dependent
                // val remarks = columnsResultSet.getString("REMARKS") 

                columns.add(com.sqldatamigration.core.models.ColumnSchema(
                    name = columnName,
                    dataType = dataType,
                    isNullable = isNullable,
                    isPrimaryKey = primaryKeyColumns.contains(columnName),
                    ordinalPosition = ordinalPosition
                ))
            }
            columnsResultSet.close()

            if (columns.isEmpty() && !tableExists) {
                 // This case should ideally be caught by the tableExists check earlier
                logger.warn("No columns found for table '$tableName', or table does not exist.")
                return null
            }
            
            logger.info("Retrieved schema for table '$tableName': ${columns.size} columns found.")
            return com.sqldatamigration.core.models.TableSchema(name = tableName, columns = columns.sortedBy { it.ordinalPosition })

        } catch (e: SQLException) {
            logger.error("Error retrieving schema for table '$tableName' from SQLite database.", e)
            throw e // Re-throw to allow higher-level handling
        }
    }

    /**
     * Extracts all data from the specified table.
     *
     * @param tableName The name of the table to extract data from.
     * @return A list of maps, where each map represents a row (column name to value).
     *         Returns an empty list if the table is empty or an error occurs.
     */
    suspend fun extractData(tableName: String): List<Map<String, Any?>> {
        if (connection == null || connection!!.isClosed) {
            throw IllegalStateException("Not connected to a database. Call connect() first.")
        }

        val data = mutableListOf<Map<String, Any?>>()
        val sql = "SELECT * FROM \"$tableName\"" // Use quotes for table names that might need it

        try {
            connection!!.createStatement().use { statement ->
                statement.executeQuery(sql).use { resultSet ->
                    val metaData = resultSet.metaData
                    val columnCount = metaData.columnCount

                    while (resultSet.next()) {
                        val rowMap = mutableMapOf<String, Any?>()
                        for (i in 1..columnCount) {
                            val columnName = metaData.getColumnLabel(i) // getColumnLabel is generally safer than getColumnName
                            rowMap[columnName] = resultSet.getObject(i)
                        }
                        data.add(rowMap)
                    }
                }
            }
            logger.info("Successfully extracted ${data.size} rows from table '$tableName'.")
        } catch (e: SQLException) {
            logger.error("Error extracting data from table '$tableName' in SQLite database.", e)
            // Depending on desired behavior, could re-throw or return empty/partial list
            // For now, returning what has been collected or an empty list if error happened early.
            return emptyList() // Or consider re-throwing: throw e
        }
        return data
    }

    /**
     * Loads data into the specified table using batch inserts and a transaction.
     *
     * @param tableName The name of the table to load data into.
     * @param data A list of maps, where each map represents a row (column name to value).
     * @return The number of rows successfully inserted.
     * @throws SQLException if a database access error occurs or the batch execution fails.
     */
    suspend fun loadData(tableName: String, data: List<Map<String, Any?>>): Int {
        if (connection == null || connection!!.isClosed) {
            throw IllegalStateException("Not connected to a database. Call connect() first.")
        }
        if (data.isEmpty()) {
            logger.info("No data provided to load into table '$tableName'.")
            return 0
        }

        // Assuming all maps in data have the same keys, representing columns.
        // Get column names from the first row.
        val firstRow = data.first()
        val columns = firstRow.keys.toList()
        if (columns.isEmpty()) {
            logger.warn("Data rows are empty (no columns) for table '$tableName'. Cannot load.")
            return 0
        }

        val placeholders = columns.joinToString(", ") { "?" }
        val columnNamesString = columns.joinToString(", ") { "\"$it\"" } // Quote column names
        val sql = "INSERT INTO \"$tableName\" ($columnNamesString) VALUES ($placeholders)"

        var rowsInserted = 0
        try {
            connection!!.prepareStatement(sql).use { pstmt ->
                for (rowMap in data) {
                    columns.forEachIndexed { index, columnName ->
                        pstmt.setObject(index + 1, rowMap[columnName])
                    }
                    pstmt.addBatch()
                }
                val batchResult = pstmt.executeBatch()
                rowsInserted = batchResult.sumOf { if (it >= 0 || it == java.sql.Statement.SUCCESS_NO_INFO) (if (it == java.sql.Statement.SUCCESS_NO_INFO) 1 else it) else 0 }
                connection!!.commit() // Commit transaction
                logger.info("Successfully loaded $rowsInserted rows into table '$tableName'.")
            }
        } catch (e: SQLException) {
            logger.error("Error loading data into table '$tableName'. Rolling back transaction.", e)
            try {
                connection?.rollback() // Rollback transaction on error
            } catch (rollbackEx: SQLException) {
                logger.error("Error during transaction rollback for table '$tableName'.", rollbackEx)
            }
            throw e // Re-throw the original exception
        }
        return rowsInserted
    }

    /**
     * Creates a new table in the database if it does not already exist, based on the provided schema.
     *
     * @param targetTableName The name of the table to create.
     * @param schema The schema definition for the table.
     * @throws SQLException if a database access error occurs.
     */
    suspend fun createTable(targetTableName: String, schema: com.sqldatamigration.core.models.TableSchema) {
        if (connection == null || connection!!.isClosed) {
            throw IllegalStateException("Not connected to a database. Call connect() first.")
        }

        val columnDefinitions = mutableListOf<String>()
        val primaryKeyColumns = schema.columns.filter { it.isPrimaryKey }.map { "\"${it.name}\"" }

        for (col in schema.columns.sortedBy { it.ordinalPosition }) {
            val colName = "\"${col.name}\""
            val colType = mapDataTypeToSqlite(col.dataType)
            var colDef = "$colName $colType"

            if (!col.isNullable) {
                colDef += " NOT NULL"
            }
            // If there's only one primary key column, define it inline.
            // Otherwise, a table-level PRIMARY KEY constraint will be added later.
            if (col.isPrimaryKey && primaryKeyColumns.size == 1) {
                colDef += " PRIMARY KEY"
                // SQLite AUTOINCREMENT is only for INTEGER PRIMARY KEY. This logic can be refined if an auto-increment flag is added to ColumnSchema.
                if (colType == "INTEGER") {
                    // colDef += " AUTOINCREMENT" // Add if explicit auto-increment is desired and supported by schema model
                }
            }
            columnDefinitions.add(colDef)
        }

        var createTableSql = "CREATE TABLE IF NOT EXISTS \"$targetTableName\" (\n"
        createTableSql += columnDefinitions.joinToString(",\n  ")

        if (primaryKeyColumns.size > 1) {
            createTableSql += ",\n  PRIMARY KEY (${primaryKeyColumns.joinToString(", ")})"
        }

        createTableSql += "\n);"

        try {
            connection!!.createStatement().use { statement ->
                statement.executeUpdate(createTableSql)
            }
            logger.info("Table '$targetTableName' created successfully or already exists.")
        } catch (e: SQLException) {
            logger.error("Error creating table '$targetTableName'. SQL: $createTableSql", e)
            throw e
        }
    }

    private fun mapDataTypeToSqlite(dataType: String): String {
        val upperType = dataType.uppercase()
        return when {
            upperType.contains("INT") -> "INTEGER"
            upperType.contains("CHAR") || upperType.contains("TEXT") || upperType.contains("CLOB") -> "TEXT"
            upperType.contains("REAL") || upperType.contains("FLOAT") || upperType.contains("DOUBLE") -> "REAL"
            upperType.contains("BLOB") -> "BLOB"
            upperType.contains("BOOL") -> "INTEGER" // SQLite uses 0 or 1 for boolean
            upperType.contains("DATE") || upperType.contains("TIME") -> "TEXT" // Store dates/times as ISO8601 strings
            // Add more specific mappings if needed
            else -> dataType // Fallback to the original type; SQLite is flexible
        }
    }
}
