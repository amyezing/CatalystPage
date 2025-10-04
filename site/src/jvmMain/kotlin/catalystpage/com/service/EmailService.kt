package catalystpage.com.service

import catalystpage.com.db.EnvConfig
import jakarta.mail.*
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import java.util.Properties


object EmailService {
    val props = Properties().apply {
        put("mail.smtp.auth", "true")
        put("mail.smtp.starttls.enable", "true") // TLS
        put("mail.smtp.host", EnvConfig.smtpHost)
        put("mail.smtp.port", EnvConfig.smtpPortTls.toString())
    }

    private val session: Session = Session.getInstance(props,
        object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(EnvConfig.smtpUser, EnvConfig.smtpPass)
            }
        })

    fun sendMail(to: String, subject: String, body: String, isHtml: Boolean = false) {
        try {
            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(EnvConfig.smtpUser, "Catalyst Support")) // nicer sender name
                setRecipients(Message.RecipientType.TO, InternetAddress.parse(to))
                setSubject(subject)

                if (isHtml) {
                    setContent(body, "text/html; charset=utf-8")
                } else {
                    setText(body)
                }
            }

            Transport.send(message)
            println("✅ Email sent to $to")
        } catch (e: Exception) {
            println("❌ Failed to send email: ${e.message}")
            e.printStackTrace()
        }
    }
}
