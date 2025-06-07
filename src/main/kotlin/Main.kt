import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.TextButton
import androidx.compose.runtime.rememberCoroutineScope
import com.sqldatamigration.core.ConfigurationManager
import kotlinx.coroutines.launch
import com.sqldatamigration.core.MigrationConfig
import com.sqldatamigration.core.connectors.SQLiteConnector
import com.sqldatamigration.core.models.DbConnectionConfig
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter
import java.io.File
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("MainKt")

fun main() = application {
    val windowState = rememberWindowState(width = 800.dp, height = 600.dp)
    logger.info("Application starting...")
    Window(
        onCloseRequest = ::exitApplication,
        state = windowState,
        title = "SqlData-Migration"
    ) {
        App()
    }
}

@Composable
@Preview
fun App() {
    val configManager = remember { ConfigurationManager() }
    var currentConfig by remember { mutableStateOf<MigrationConfig?>(null) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var selectedDbFilePath by remember { mutableStateOf<String?>(null) }
    val sqliteConnector = remember { SQLiteConnector() }
    var sourceTables by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedSourceTable by remember { mutableStateOf<String?>(null) }
    var targetTableNameInput by remember { mutableStateOf("") }
    var expandedDropdown by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    MaterialTheme {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("SQL Data Migration Tool", style = MaterialTheme.typography.h5)

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                val fileChooser = JFileChooser().apply {
                    dialogTitle = "Select SQLite Database File"
                    fileSelectionMode = JFileChooser.FILES_ONLY
                    val filter = FileNameExtensionFilter("SQLite DB (*.db, *.sqlite, *.sqlite3)", "db", "sqlite", "sqlite3")
                    addChoosableFileFilter(filter)
                    isAcceptAllFileFilterUsed = false
                }
                if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    selectedDbFilePath = fileChooser.selectedFile.absolutePath
                    logger.info("Selected SQLite file: $selectedDbFilePath")
                    statusMessage = "Selected DB file: ${fileChooser.selectedFile.name}"
                }
            }) {
                Text("Select Source SQLite DB File")
            }
            selectedDbFilePath?.let {
                Text("Source DB: $it", style = MaterialTheme.typography.caption)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Migration Setup", style = MaterialTheme.typography.h6)

            Button(onClick = {
                selectedDbFilePath?.let {
                    coroutineScope.launch {
                        try {
                            statusMessage = "Loading tables..."
                            sqliteConnector.connect(DbConnectionConfig.SQLiteConfig(it))
                            val tables = sqliteConnector.getTables()
                            sourceTables = tables
                            selectedSourceTable = null // Reset selection
                            targetTableNameInput = "" // Reset target name
                            statusMessage = if (tables.isNotEmpty()) "Tables loaded from source DB." else "No tables found in source DB."
                            logger.info("Loaded tables: $tables")
                        } catch (e: Exception) {
                            statusMessage = "Error loading tables: ${e.message}"
                            logger.error("Error loading tables from $it", e)
                            sourceTables = emptyList()
                        } finally {
                            // Consider if disconnect should happen here or be managed more globally
                            // sqliteConnector.disconnect() 
                        }
                    }
                } ?: run {
                    statusMessage = "Please select a source SQLite DB file first."
                }
            }, enabled = selectedDbFilePath != null) {
                Text("Load Tables from Source DB")
            }

            if (sourceTables.isNotEmpty()) {
                Box {
                    TextButton(onClick = { expandedDropdown = true }) {
                        Text(selectedSourceTable ?: "Select Source Table")
                    }
                    DropdownMenu(
                        expanded = expandedDropdown,
                        onDismissRequest = { expandedDropdown = false }
                    ) {
                        sourceTables.forEach { table ->
                            DropdownMenuItem(onClick = {
                                selectedSourceTable = table
                                targetTableNameInput = "${table}_migrated" // Suggest a default target name
                                expandedDropdown = false
                            }) {
                                Text(table)
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = targetTableNameInput,
                    onValueChange = { targetTableNameInput = it },
                    label = { Text("Target Table Name") },
                    singleLine = true,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Button(
                    onClick = {
                        if (selectedSourceTable != null && targetTableNameInput.isNotBlank() && selectedDbFilePath != null) {
                            val sourceDb = selectedDbFilePath!!
                            val srcTable = selectedSourceTable!!
                            val tgtTable = targetTableNameInput

                            coroutineScope.launch {
                                statusMessage = "Migration starting for '$srcTable' to '$tgtTable'..."
                                logger.info("Attempting migration: $sourceDb table '$srcTable' to '$tgtTable'")
                                try {
                                    // Ensure connection (connect might be called again, but it's fine)
                                    sqliteConnector.connect(DbConnectionConfig.SQLiteConfig(sourceDb))

                                    // 1. Get schema of source table
                                    logger.info("Fetching schema for source table '$srcTable'...")
                                    val schema = sqliteConnector.getSchema(srcTable)
                                    if (schema == null) {
                                        statusMessage = "Error: Could not get schema for source table '$srcTable'."
                                        logger.error("Migration failed: Schema not found for $srcTable")
                                        return@launch
                                    }
                                    logger.info("Schema for '$srcTable' retrieved successfully.")

                                    // 2. Create target table (IF NOT EXISTS)
                                    logger.info("Creating target table '$tgtTable'...")
                                    sqliteConnector.createTable(tgtTable, schema)
                                    logger.info("Target table '$tgtTable' created or already exists.")

                                    // 3. Extract data from source table
                                    logger.info("Extracting data from '$srcTable'...")
                                    val data = sqliteConnector.extractData(srcTable)
                                    if (data.isEmpty() && schema.columns.isNotEmpty()) {
                                        // Table might be empty, which is not an error itself
                                        logger.info("Source table '$srcTable' is empty or no data extracted.")
                                    } else {
                                        logger.info("Extracted ${data.size} rows from '$srcTable'.")
                                    }

                                    // 4. Load data into target table
                                    if (data.isNotEmpty()){
                                        logger.info("Loading data into '$tgtTable'...")
                                        val rowsInserted = sqliteConnector.loadData(tgtTable, data)
                                        statusMessage = "Migration successful: $rowsInserted rows transferred from '$srcTable' to '$tgtTable'."
                                        logger.info("Migration successful: $rowsInserted rows loaded into '$tgtTable'.")
                                    } else {
                                        statusMessage = "Migration complete: Source table '$srcTable' was empty. Target table '$tgtTable' created."
                                        logger.info("Migration complete: Source table '$srcTable' was empty. Target table '$tgtTable' created.")
                                    }

                                } catch (e: Exception) {
                                    statusMessage = "Migration failed: ${e.message}"
                                    logger.error("Migration error from '$srcTable' to '$tgtTable' in $sourceDb", e)
                                } finally {
                                    // Decide on disconnect strategy. For now, leave connection open if it was opened.
                                    // sqliteConnector.disconnect() 
                                }
                            }
                        } else {
                            statusMessage = "Please select source DB, source table, and specify target table name."
                        }
                    },
                    enabled = selectedSourceTable != null && targetTableNameInput.isNotBlank(),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Start SQLite to SQLite Migration")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Configuration Management", style = MaterialTheme.typography.h6)

            Button(onClick = {
                val fileChooser = JFileChooser().apply {
                    dialogTitle = "Load Migration Configuration"
                    fileSelectionMode = JFileChooser.FILES_ONLY
                    val filter = FileNameExtensionFilter("JSON config (*.json)", "json")
                    addChoosableFileFilter(filter)
                    isAcceptAllFileFilterUsed = false
                }
                if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    val file = fileChooser.selectedFile
                    val loadedConfig = configManager.loadConfig(file.absolutePath)
                    if (loadedConfig != null) {
                        currentConfig = loadedConfig
                        statusMessage = "Configuration loaded: ${file.name}"
                        logger.info("Loaded configuration from ${file.absolutePath}")
                    } else {
                        statusMessage = "Failed to load configuration from ${file.name}"
                        logger.warn("Failed to load configuration from ${file.absolutePath}")
                    }
                }
            }) {
                Text("Load Configuration (.json)")
            }

            Button(onClick = {
                val fileChooser = JFileChooser().apply {
                    dialogTitle = "Save Migration Configuration"
                    fileSelectionMode = JFileChooser.FILES_ONLY
                    val filter = FileNameExtensionFilter("JSON config (*.json)", "json")
                    addChoosableFileFilter(filter)
                    isAcceptAllFileFilterUsed = false
                    selectedFile = File("migration_config.json") // Default file name
                }
                if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                    var fileToSave = fileChooser.selectedFile
                    // Ensure .json extension
                    if (!fileToSave.name.endsWith(".json", ignoreCase = true)) {
                        fileToSave = File(fileToSave.parentFile, fileToSave.nameWithoutExtension + ".json")
                    }
                    
                    val configToSave = currentConfig ?: MigrationConfig(
                        projectName = "New Migration Project",
                        description = "Default configuration. Please update source/target.",
                        sourceConfig = DbConnectionConfig.SQLiteConfig(selectedDbFilePath ?: "path/to/source.db"), // Use selected DB if available
                        targetConfig = DbConnectionConfig.SQLiteConfig("path/to/target.db"),
                        version = "1.0"
                    )

                    if (configManager.saveConfig(configToSave, fileToSave.absolutePath)) {
                        currentConfig = configToSave // Update current config if it was default
                        statusMessage = "Configuration saved: ${fileToSave.name}"
                        logger.info("Saved configuration to ${fileToSave.absolutePath}")
                    } else {
                        statusMessage = "Failed to save configuration to ${fileToSave.name}"
                        logger.warn("Failed to save configuration to ${fileToSave.absolutePath}")
                    }
                }
            }) {
                Text("Save Configuration (.json)")
            }

            Spacer(modifier = Modifier.height(8.dp))

            currentConfig?.let {
                Text("Loaded Project: ${it.projectName ?: "N/A"}", style = MaterialTheme.typography.subtitle1)
                // Add more details from config if needed, e.g., it.description
            }

            statusMessage?.let {
                Text(it, style = MaterialTheme.typography.body2, color = if (it.startsWith("Failed")) MaterialTheme.colors.error else MaterialTheme.colors.onSurface)
            }
        }
    }
}
