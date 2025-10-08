package catalystpage.com.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import java.sql.Connection

object DbConnection {
    private var dataSource: HikariDataSource? = null

    fun connect() {
        try {
            val config = HikariConfig().apply {
                jdbcUrl = "jdbc:mariadb://${EnvConfig.dbHost}:${EnvConfig.dbPort}/${EnvConfig.dbName}"
                username = EnvConfig.dbUser
                password = EnvConfig.dbPass
                driverClassName = "org.mariadb.jdbc.Driver"
                maximumPoolSize = 10
                isAutoCommit = false
                transactionIsolation = "TRANSACTION_REPEATABLE_READ"
                // REMOVE validate() - it might be throwing the exception
            }

            dataSource = HikariDataSource(config)
            Database.connect(dataSource!!)
            println("✅ Database connected successfully")
        } catch (e: Exception) {
            println("⚠️ Database connection failed: ${e.message}")
            // CRITICAL: Don't rethrow the exception!
            // The app should continue without database
        }
    }

    fun isConnected(): Boolean {
        return dataSource != null && try {
            dataSource!!.connection.use {
                // Test if connection is actually working
                true
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