package catalystpage.com.api


import catalystpage.com.db.EnvConfig
import com.varabyte.kobweb.api.Api
import com.varabyte.kobweb.api.ApiContext
import java.sql.DriverManager

@Api
fun time(context: ApiContext) {
    val url = "jdbc:mariadb://${EnvConfig.dbHost}:${EnvConfig.dbPort}/${EnvConfig.dbName}"
    val user = EnvConfig.dbUser
    val password = EnvConfig.dbPass

    try {
        DriverManager.getConnection(url, user, password).use { conn ->
            val stmt = conn.createStatement()
            val rs = stmt.executeQuery("SELECT NOW()")
            val message = if (rs.next()) {
                "Time from DB: ${rs.getString(1)}"
            } else {
                "No data returned"
            }
            context.res.status = 200
            context.res.contentType = "text/plain"
            context.res.body = message.encodeToByteArray()
        }
    } catch (e: Exception) {
        context.res.status = 500
        context.res.contentType = "text/plain"
        context.res.body = "DB error: ${e.message}".encodeToByteArray()
    }
}
