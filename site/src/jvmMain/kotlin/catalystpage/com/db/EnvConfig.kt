package catalystpage.com.db

import io.github.cdimascio.dotenv.Dotenv
import java.io.File

object EnvConfig {

    private val dotenv: Dotenv = Dotenv.configure()
        .directory(File(System.getProperty("user.dir")).parentFile.absolutePath)// force root dir
        .ignoreIfMissing()
        .load()

    val smtpHost: String = dotenv["SMTP_HOST"] ?: "smtpout.secureserver.net"
    val smtpPortTls: Int = dotenv["SMTP_PORT_TLS"]?.toInt() ?: 587
    val smtpPortSsl: Int = dotenv["SMTP_PORT_SSL"]?.toInt() ?: 465
    val smtpUser: String = (dotenv["SMTP_USER"] ?: "").trim()
    val smtpPass: String = (dotenv["SMTP_PASS"] ?: "").trim()
    //cloud
    val gcsCredentialsPath: String = dotenv["GCS_CREDENTIALS_PATH"]
        ?: throw RuntimeException("GCS_CREDENTIALS_PATH not set in .env")
    val gcsBucketName: String = dotenv["GCS_BUCKET_NAME"]
        ?: throw RuntimeException("GCS_BUCKET_NAME not set in .env")


    //db
    val dbUser: String = (dotenv["DB_USER"] ?: "").trim()
    val dbPass: String = (dotenv["DB_PASS"] ?: "").trim()
    val dbHost: String = (dotenv["DB_HOST"] ?: "localhost").trim()
    val dbPort: Int = (dotenv["DB_PORT"]?.toInt() ?: 3306)
    val dbName: String = (dotenv["DB_NAME"] ?: "catalystdb").trim()


    //firebase
    val firebaseApiKey = dotenv["FIREBASE_API_KEY"] ?: ""
    val firebaseAuthDomain = dotenv["FIREBASE_AUTH_DOMAIN"] ?: ""
    val firebaseProjectId = dotenv["FIREBASE_PROJECT_ID"] ?: ""
    val firebaseStorageBucket = dotenv["FIREBASE_STORAGE_BUCKET"] ?: ""
    val firebaseMessagingSenderId = dotenv["FIREBASE_MESSAGING_SENDER_ID"] ?: ""
    val firebaseAppId = dotenv["FIREBASE_APP_ID"] ?: ""

    //admins
    val adminEmails: List<String> = dotenv["ADMIN_EMAILS"]
        ?.split(",")
        ?.map { it.trim() }
        ?: emptyList()

}



