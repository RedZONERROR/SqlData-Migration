import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm") version "1.9.23"
    id("org.jetbrains.compose") version "1.6.2"
    kotlin("plugin.serialization") version "1.9.23"
}

group = "com.sqldatamigration"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

kotlin {
    jvmToolchain(17)
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Rpm)
            packageName = "SqlDataMigration"
            packageVersion = project.version.toString()

            windows {
                // menuGroup = "MyCompany"
                // shortcut = true
                // exePackageVersion = project.version.toString()
            }
            macOS {
                // bundleID = "com.sqldatamigration"
            }
            linux {
                // packageName = "sqldatamigration"
            }
        }
    }
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(kotlin("stdlib"))

    // Logging - SLF4J API and Logback Classic implementation
    implementation("org.slf4j:slf4j-api:2.0.12") // Or latest SLF4J version
    implementation("ch.qos.logback:logback-classic:1.5.6") // Or latest Logback version

    // JDBC Drivers
    implementation("org.xerial:sqlite-jdbc:3.45.3.0") // SQLite JDBC Driver

    // Kotlinx Serialization for JSON
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
}
