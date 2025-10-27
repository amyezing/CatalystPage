package catalystpage.com.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import java.sql.Connection

object DbConnection {
    private var dataSource: HikariDataSource? = null

    fun connect() {
        var connected = false
        var attempts = 0
        val maxAttempts = 5
        val retryDelayMs = 5000L // 5 seconds

        while (!connected && attempts < maxAttempts) {
            attempts++
            try {
                println("🔗 Database connection attempt $attempts/$maxAttempts...")

                val isCloudRun = System.getenv("K_SERVICE") != null

                val config = HikariConfig().apply {
                    if (isCloudRun) {
                        // Cloud SQL MySQL configuration
                        jdbcUrl = "jdbc:mysql:///${EnvConfig.dbName}?socketFactory=com.google.cloud.sql.mysql.SocketFactory&cloudSqlInstance=${EnvConfig.dbHost}"
                        driverClassName = "com.mysql.cj.jdbc.Driver"
                    } else {
                        // Local MariaDB configuration
                        jdbcUrl = "jdbc:mariadb://${EnvConfig.dbHost}:${EnvConfig.dbPort}/${EnvConfig.dbName}"
                        driverClassName = "org.mariadb.jdbc.Driver"
                    }

                    username = EnvConfig.dbUser
                    password = EnvConfig.dbPass
                    maximumPoolSize = 10
                    isAutoCommit = false
                    transactionIsolation = "TRANSACTION_REPEATABLE_READ"

                    // Additional connection validation settings
                    connectionTimeout = 30000 // 30 seconds
                    validationTimeout = 5000
                    leakDetectionThreshold = 60000
                }

                dataSource = HikariDataSource(config)

                // Test the connection immediately
                dataSource!!.connection.use { conn ->
                    if (conn.isValid(2)) {
                        Database.connect(dataSource!!)
                        connected = true
                        println("✅ Database connected successfully on attempt $attempts (${if (isCloudRun) "Cloud SQL" else "Local MariaDB"})")
                    }
                }

            } catch (e: Exception) {
                println("❌ Database connection attempt $attempts failed: ${e.message}")

                // Clean up failed connection
                dataSource?.close()
                dataSource = null

                if (attempts < maxAttempts) {
                    println("⏳ Retrying in ${retryDelayMs/1000} seconds...")
                    Thread.sleep(retryDelayMs)
                }
            }
        }

        if (!connected) {
            println("💥 All database connection attempts failed after $maxAttempts attempts")
            throw RuntimeException("Failed to connect to database after $maxAttempts attempts")
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