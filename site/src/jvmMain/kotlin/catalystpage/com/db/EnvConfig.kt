package catalystpage.com.db

import io.github.cdimascio.dotenv.Dotenv
import kotlinx.serialization.json.Json
import java.io.File

object EnvConfig {
    private val dotenv: Dotenv? = try {
        Dotenv.configure()
            .directory(File(System.getProperty("user.dir")).parentFile.absolutePath)
            .ignoreIfMissing()
            .load()
    } catch (e: Exception) {
        println("Warning: .env file not found, using environment variables only")
        null
    }

    // Helper function to read from environment variables first, then .env
    private fun getConfig(key: String): String? {
        // Try environment variable first (for Cloud Run)
        System.getenv(key)?.let { return it }
        // Fall back to .env file (for local development)
        return dotenv?.get(key)
    }

    // Database configuration
    val dbUser: String = getConfig("DB_USER") ?: ""
    val dbPass: String = getConfig("DB_PASS") ?: ""
    val dbHost: String = getConfig("DB_HOST") ?: "localhost"
    val dbPort: Int = try {
        val portConfig = getConfig("DB_PORT")
        println("🔍 DEBUG: DB_PORT value from config: '$portConfig'")
        when {
            portConfig == null -> 3306
            portConfig.toIntOrNull() != null -> portConfig.toInt()
            else -> {
                println("❌ WARNING: Invalid DB_PORT '$portConfig', using default 3306")
                3306
            }
        }
    } catch (e: Exception) {
        println("❌ ERROR in DB_PORT config: ${e.message}, using default 3306")
        3306
    }
    val dbName: String = getConfig("DB_NAME") ?: "catalystdb"


    private val firebaseConfig: Map<String, String> by lazy {
        getConfig("FIREBASE_CONFIG")?.let { jsonString ->
            try {
                Json.decodeFromString<Map<String, String>>(jsonString)
            } catch (e: Exception) {
                emptyMap()
            }
        } ?: emptyMap()
    }
    // Firebase configuration
    val firebaseApiKey: String = firebaseConfig["apiKey"] ?: ""
    val firebaseAuthDomain: String = firebaseConfig["authDomain"] ?: ""
    val firebaseProjectId: String = firebaseConfig["projectId"] ?: ""
    val firebaseStorageBucket: String = firebaseConfig["storageBucket"] ?: ""
    val firebaseMessagingSenderId: String = firebaseConfig["messagingSenderId"] ?: ""
    val firebaseAppId: String = firebaseConfig["appId"] ?: ""

    // GCS configuration - handle required fields
    val gcsBucketName: String = getConfig("GCS_BUCKET_NAME")
        ?: throw RuntimeException("GCS_BUCKET_NAME not set in environment or .env")

    val gcsCredentialsPath: String = getConfig("GCS_CREDENTIALS_PATH") ?: ""

    // SMTP configuration
    val smtpHost: String = getConfig("SMTP_HOST") ?: "smtpout.secureserver.net"
    val smtpPortTls: Int = getConfig("SMTP_PORT_TLS")?.toInt() ?: 587
    val smtpPortSsl: Int = getConfig("SMTP_PORT_SSL")?.toInt() ?: 465
    val smtpUser: String = getConfig("SMTP_USER") ?: ""
    val smtpPass: String = getConfig("SMTP_PASS") ?: ""

    // Admins
    val adminEmails: List<String> = getConfig("ADMIN_EMAILS")
        ?.split(",")
        ?.map { it.trim() }
        ?: emptyList()
}



