package catalystpage.com.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import java.sql.Connection

object DbConnection {
    private var dataSource: HikariDataSource? = null

    fun connect() {
        try {
            val isCloudRun = System.getenv("K_SERVICE") != null

            val config = HikariConfig().apply {
                if (isCloudRun) {
                    // Cloud SQL MySQL configuration - CORRECT FORMAT
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
            }

            dataSource = HikariDataSource(config)
            Database.connect(dataSource!!)
            println("✅ Database connected successfully (${if (isCloudRun) "Cloud SQL" else "Local MariaDB"})")
        } catch (e: Exception) {
            println("⚠️ Database connection failed: ${e.message}")
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
}