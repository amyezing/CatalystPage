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

            val config = HikariConfig().apply {
                // Use localhost via Cloud SQL Auth Proxy
                jdbcUrl = "jdbc:mysql://localhost:3306/catalystdb?" +
                    "useSSL=false&" +
                    "allowPublicKeyRetrieval=true&" +
                    "defaultAuthenticationPlugin=mysql_native_password&" +
                    "connectTimeout=10000&" +
                    "socketTimeout=30000"
                driverClassName = "com.mysql.cj.jdbc.Driver"
                println("🔍 DEBUG: Using localhost via Cloud SQL Auth Proxy")

                username = "admin"
                password = "${EnvConfig.dbPass}"
                maximumPoolSize = 3
                minimumIdle = 1
                connectionTimeout = 20000  // 20 seconds
                validationTimeout = 10000
            }

            dataSource = HikariDataSource(config)

            // Test connection
            dataSource!!.connection.use { conn ->
                if (conn.isValid(5)) {
                    Database.connect(dataSource!!)
                    println("✅ Database connected successfully")
                }
            }

        } catch (e: Exception) {
            println("❌ Database connection failed: ${e.message}")
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
