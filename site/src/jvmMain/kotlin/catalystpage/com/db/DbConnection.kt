package catalystpage.com.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import java.sql.Connection

object DbConnection {
    private var dataSource: HikariDataSource? = null

    fun connect() {
        try {
            println("🔗 Attempting database connection...")

            val isCloudRun = System.getenv("K_SERVICE") != null
            println("🔍 DEBUG: isCloudRun = $isCloudRun")
            
            val config = HikariConfig().apply {
                if (isCloudRun) {
                    // Correct Cloud SQL connection format
                    jdbcUrl = "jdbc:mysql:///catalystdb?unixSocket=/cloudsql/ethereal-zodiac-454604-u2:asia-southeast1:catalyst-db"
                    driverClassName = "com.mysql.cj.jdbc.Driver"
                    println("🔍 DEBUG: Using Cloud SQL Unix socket")
                } else {
                    jdbcUrl = "jdbc:mariadb://${EnvConfig.dbHost}:${EnvConfig.dbPort}/${EnvConfig.dbName}"
                    driverClassName = "org.mariadb.jdbc.Driver"
                }

                username = "admin"
                password = "${EnvConfig.dbPass}"
                maximumPoolSize = 3
                minimumIdle = 1
                
                // Reduced timeouts for faster failure
                connectionTimeout = 10000
                validationTimeout = 5000
            }

            dataSource = HikariDataSource(config)

            // Test connection
            dataSource!!.connection.use { conn ->
                if (conn.isValid(2)) {
                    Database.connect(dataSource!!)
                    println("✅ Database connected successfully")
                }
            }

        } catch (e: Exception) {
            println("❌ Database connection failed: ${e.message}")
            e.printStackTrace()
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
