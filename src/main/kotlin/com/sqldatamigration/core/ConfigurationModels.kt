package com.sqldatamigration.core

import com.sqldatamigration.core.models.DbConnectionConfig // Keep this if DbConnectionConfig is in a sub-package or different file
import kotlinx.serialization.Serializable

// Placeholder for future detailed table and column mappings
// @Serializable
// data class ColumnMapping(
//     val sourceColumn: String,
//     val targetColumn: String,
//     // val transformation: String? = null // Future: for data transformation rules
// )

// @Serializable
// data class TableMapping(
//     val sourceTable: String,
//     val targetTable: String,
//     val columns: List<ColumnMapping> = emptyList()
// )

@Serializable
data class MigrationConfig(
    val sourceConfig: DbConnectionConfig,
    val targetConfig: DbConnectionConfig,
    // val tableMappings: List<TableMapping> = emptyList(), // To be detailed in a later phase
    val projectName: String? = null, // Optional project name
    val description: String? = null, // Optional description for the configuration
    val version: String = "1.0" // Configuration file format version
)
