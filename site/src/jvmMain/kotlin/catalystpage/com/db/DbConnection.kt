package catalystpage.com.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import java.sql.Connection

object DbConnection {
    private var dataSource: HikariDataSource? = null
    private val connectionLock = Any()
    private var isConnecting = false

    fun connect() {
        synchronized(connectionLock) {
            // If already connected or connecting, return
            if (dataSource != null || isConnecting) {
                println("🔗 Database connection already established or in progress")
                return
            }

            isConnecting = true
        }

        var connected = false
        var attempts = 0
        val maxAttempts = 5
        val retryDelayMs = 5000L

        while (!connected && attempts < maxAttempts) {
            attempts++
            try {
                println("🔗 Database connection attempt $attempts/$maxAttempts...")

                val isCloudRun = System.getenv("K_SERVICE") != null
                println("🔍 DEBUG: isCloudRun = $isCloudRun")
                println("🔍 DEBUG: dbHost = '${EnvConfig.dbHost}'")
                println("🔍 DEBUG: dbUser = '${EnvConfig.dbUser}'")
                println("🔍 DEBUG: dbName = '${EnvConfig.dbName}'")

                val config = HikariConfig().apply {
                    if (isCloudRun) {
                        jdbcUrl = "jdbc:mysql:///${EnvConfig.dbName}?socketFactory=com.google.cloud.sql.mysql.SocketFactory&cloudSqlInstance=${EnvConfig.dbHost}"
                        driverClassName = "com.mysql.cj.jdbc.Driver"
                        println("🔍 DEBUG: Using Cloud SQL Socket Factory")
                    } else {
                        jdbcUrl = "jdbc:mariadb://${EnvConfig.dbHost}:${EnvConfig.dbPort}/${EnvConfig.dbName}"
                        driverClassName = "org.mariadb.jdbc.Driver"
                    }

                    username = EnvConfig.dbUser
                    password = EnvConfig.dbPass
                    maximumPoolSize = 3  // Reduced for Cloud Run
                    minimumIdle = 1
                    isAutoCommit = false
                    transactionIsolation = "TRANSACTION_REPEATABLE_READ"

                    connectionTimeout = 30000
                    validationTimeout = 5000
                    leakDetectionThreshold = 60000
                }

                dataSource = HikariDataSource(config)

                // Test connection
                dataSource!!.connection.use { conn ->
                    if (conn.isValid(2)) {
                        Database.connect(dataSource!!)
                        connected = true
                        isConnecting = false
                        println("✅ Database connected successfully on attempt $attempts")
                    }
                }

            } catch (e: Exception) {
                println("❌ Database connection attempt $attempts failed: ${e.message}")
                e.printStackTrace()  // Add stack trace for more details

                dataSource?.close()
                dataSource = null

                if (attempts < maxAttempts) {
                    println("⏳ Retrying in ${retryDelayMs/1000} seconds...")
                    Thread.sleep(retryDelayMs)
                }
            }
        }

        if (!connected) {
            isConnecting = false
            println("💥 All database connection attempts failed after $maxAttempts attempts")
        }
    }

    fun isConnected(): Boolean {
        return dataSource != null && try {
            dataSource!!.connection.use { conn ->
                conn.isValid(2)
            }
        } catch (e: Exception) {
            false
        }
    }

    fun getConnection(): Connection? {
        return try {
            dataSource?.connection
        } catch (e: Exception) {
            null
        }
    }

    fun close() {
        dataSource?.close()
        dataSource = null
        println("🔌 Database connection closed")
    }
}