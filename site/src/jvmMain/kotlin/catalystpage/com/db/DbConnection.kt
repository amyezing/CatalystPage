package catalystpage.com.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database

object DbConnection {
    fun connect() {
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

        val dataSource = HikariDataSource(config)
        Database.connect(dataSource)
    }
}