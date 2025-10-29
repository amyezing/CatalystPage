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
                
                val config = HikariConfig().apply {
                    if (isCloudRun) {
                        // Use direct IP connection - we know this works from manual testing
                        jdbcUrl = "jdbc:mysql://34.87.24.135:3306/catalystdb?" +
                            "useSSL=false&" +
                            "allowPublicKeyRetrieval=true&" +
                            "defaultAuthenticationPlugin=mysql_native_password&" +
                            "connectTimeout=5000&" +
                            "socketTimeout=30000"
                        driverClassName = "com.mysql.cj.jdbc.Driver"
                        println("🔍 DEBUG: Using direct IP connection to Cloud SQL")
                    } else {
                        jdbcUrl = "jdbc:mariadb://${EnvConfig.dbHost}:${EnvConfig.dbPort}/${EnvConfig.dbName}"
                        driverClassName = "org.mariadb.jdbc.Driver"
                    }

                    username = "admin"
                    password = "${EnvConfig.dbPass}"
                    maximumPoolSize = 3
                    minimumIdle = 1
                    isAutoCommit = false
                    transactionIsolation = "TRANSACTION_REPEATABLE_READ"

                    // Timeout settings
                    connectionTimeout = 10000
                    validationTimeout = 5000
                    leakDetectionThreshold = 60000
                }

                dataSource = HikariDataSource(config)

                // Test connection
                dataSource!!.connection.use { conn ->
                    if (conn.isValid(5)) {
                        Database.connect(dataSource!!)
                        connected = true
                        isConnecting = false
                        println("✅ Database connected successfully on attempt $attempts")
                    }
                }

            } catch (e: Exception) {
                println("❌ Database connection attempt $attempts failed: ${e.message}")
                e.printStackTrace()

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
        println("Database connection closed")
    }
}
