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
        System.getenv(key)?.let {
            println("✅ Loaded $key from environment")
            return it
        }
        // Fall back to .env file (for local development)
        return dotenv?.get(key)?.also {
            println("✅ Loaded $key from .env file")
        }
    }

    private fun getRequiredConfig(key: String): String {
        return getConfig(key) ?: throw RuntimeException("Required configuration '$key' not found")
    }

    // Database configuration - support both DB_PASS and DB_PASSWORD for compatibility
    val dbUser: String = getRequiredConfig("DB_USER")
    val dbPass: String = getConfig("DB_PASS") ?: getRequiredConfig("DB_PASSWORD") // Support both
    val dbHost: String = getRequiredConfig("DB_HOST")
    val dbPort: Int = getConfig("DB_PORT")?.toIntOrNull() ?: 3306
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

    // GCS configuration
    val gcsBucketName: String = getRequiredConfig("GCS_BUCKET_NAME")
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

    init {
        println("=".repeat(50))
        println("📋 CONFIGURATION LOADED SUCCESSFULLY")
        println("=".repeat(50))
        println("Database: $dbHost:$dbPort/$dbName")
        println("GCS Bucket: $gcsBucketName")
        println("Environment variables loaded successfully")
        println("=".repeat(50))
    }
}