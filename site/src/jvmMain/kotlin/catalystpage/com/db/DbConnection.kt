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
                validate()
            }

            dataSource = HikariDataSource(config)
            Database.connect(dataSource!!)
            println("✅ Database connected successfully")
        } catch (e: Exception) {
            println("⚠️ Database connection failed: ${e.message}")
            // Don't rethrow - just log and continue
        }
    }

    fun isConnected(): Boolean {
        return dataSource != null && try {
            dataSource!!.connection.use { true }
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