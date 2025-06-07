package com.sqldatamigration.core

import com.sqldatamigration.core.models.DbConnectionConfig // For MigrationConfig's properties
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import kotlinx.serialization.SerializationException

class ConfigurationManager {

    private val logger = LoggerFactory.getLogger(ConfigurationManager::class.java)

    // Configure a lenient Json parser instance if needed, e.g., to ignore unknown keys
    private val json = Json { 
        isLenient = true 
        ignoreUnknownKeys = true 
        prettyPrint = true // For saving, but good to have consistent settings
        classDiscriminator = "type" // Important if DbConnectionConfig subtypes are directly in JSON
    }

    /**
     * Loads a migration configuration from a JSON file.
     *
     * @param filePath The path to the configuration file.
     * @return A [MigrationConfig] object if loading is successful, or null otherwise.
     */
    fun loadConfig(filePath: String): MigrationConfig? {
        val configFile = File(filePath)
        if (!configFile.exists() || !configFile.isFile) {
            logger.error("Configuration file not found or is not a file: {}", filePath)
            return null
        }

        return try {
            val configJsonString = configFile.readText()
            val config = json.decodeFromString<MigrationConfig>(configJsonString)
            logger.info("Successfully loaded configuration from: {}", filePath)
            config
        } catch (e: IOException) {
            logger.error("Error reading configuration file {}: {}", filePath, e.message)
            null
        } catch (e: SerializationException) {
            logger.error("Error parsing configuration file {}: {}", filePath, e.message)
            null
        } catch (e: Exception) {
            logger.error("An unexpected error occurred while loading configuration from {}: {}", filePath, e.message, e)
            null
        }
    }

    /**
     * Saves a migration configuration to a JSON file.
     *
     * @param config The [MigrationConfig] object to save.
     * @param filePath The path to the file where the configuration will be saved.
     * @return True if saving is successful, false otherwise.
     */
    fun saveConfig(config: MigrationConfig, filePath: String): Boolean {
        val configFile = File(filePath)
        return try {
            // Ensure parent directory exists
            configFile.parentFile?.mkdirs()
            
            val configJsonString = json.encodeToString(config)
            configFile.writeText(configJsonString)
            logger.info("Successfully saved configuration to: {}", filePath)
            true
        } catch (e: IOException) {
            logger.error("Error writing configuration file {}: {}", filePath, e.message)
            false
        } catch (e: SerializationException) {
            logger.error("Error serializing configuration for {}: {}", filePath, e.message)
            false
        } catch (e: Exception) {
            logger.error("An unexpected error occurred while saving configuration to {}: {}", filePath, e.message, e)
            false
        }
    }
}
