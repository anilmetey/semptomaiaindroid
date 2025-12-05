package com.semptom.ai.data.email

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Properties
import javax.inject.Inject
import javax.inject.Singleton
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.MessagingException
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

@Singleton
class EmailService @Inject constructor() {
    private val TAG = "EmailService"

    // Gmail SMTP ayarları
    private val host = "smtp.gmail.com"
    private val port = "587" // TLS için 587, SSL için 465

    // Test hesabı bilgileri (Güvenlik için bu bilgileri SharedPreferences'tan alın)
    private var senderEmail = "semptomai@gmail.com" // senin Gmail
    private var senderPassword = "vgwmibhgvmafxyrm" // App Password (boşluksuz)

    fun updateCredentials(email: String, password: String) {
        senderEmail = email
        senderPassword = password
        Log.d(TAG, "Email credentials updated for: $email")
    }

    fun hasCredentials(): Boolean {
        return senderEmail.isNotBlank() && senderPassword.isNotBlank()
    }

    suspend fun sendVerificationCode(recipientEmail: String, code: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Attempting to send verification code to: $recipientEmail")

                // SMTP özellikleri
                val properties = Properties().apply {
                    put("mail.smtp.auth", "true")
                    put("mail.smtp.starttls.enable", "true")
                    put("mail.smtp.host", host)
                    put("mail.smtp.port", port)
                    put("mail.smtp.ssl.protocols", "TLSv1.2")
                    put("mail.smtp.socketFactory.port", port)
                    put("mail.smtp.socketFactory.fallback", "false")
                }

                // Session oluştur
                val session = Session.getInstance(properties, object : Authenticator() {
                    override fun getPasswordAuthentication(): PasswordAuthentication {
                        return PasswordAuthentication(senderEmail, senderPassword)
                    }
                })

                // Debug modunu aç (geliştirme sırasında)
                session.debug = true

                // Mesajı oluştur
                val message = MimeMessage(session).apply {
                    setFrom(InternetAddress(senderEmail, "SemptomAI"))
                    setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail))
                    subject = "SemptomAI - Doğrulama Kodu"

                    // HTML formatında e-posta içeriği
                    setContent(createEmailContent(code), "text/html; charset=utf-8")
                }

                // E-postayı gönder
                Transport.send(message)

                Log.d(TAG, "Verification code sent successfully to: $recipientEmail")
                true

            } catch (e: MessagingException) {
                Log.e(TAG, "MessagingException while sending email", e)
                Log.e(TAG, "Error details: ${e.message}")
                false
            } catch (e: Exception) {
                Log.e(TAG, "Exception while sending email", e)
                Log.e(TAG, "Error details: ${e.message}")
                false
            }
        }
    }

    private fun createEmailContent(code: String): String {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body {
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        background-color: #f5f5f5;
                        margin: 0;
                        padding: 0;
                    }
                    .container {
                        max-width: 600px;
                        margin: 40px auto;
                        background-color: white;
                        border-radius: 16px;
                        overflow: hidden;
                        box-shadow: 0 4px 12px rgba(0,0,0,0.1);
                    }
                    .header {
                        background: linear-gradient(135deg, #1E3A8A 0%, #3B82F6 100%);
                        padding: 40px 20px;
                        text-align: center;
                        color: white;
                    }
                    .header h1 {
                        margin: 0;
                        font-size: 28px;
                        font-weight: 700;
                    }
                    .content {
                        padding: 40px 30px;
                    }
                    .code-box {
                        background: linear-gradient(135deg, #EEF2FF 0%, #E0E7FF 100%);
                        border: 2px solid #3B82F6;
                        border-radius: 12px;
                        padding: 30px;
                        text-align: center;
                        margin: 30px 0;
                    }
                    .code {
                        font-size: 42px;
                        font-weight: 900;
                        color: #1E3A8A;
                        letter-spacing: 8px;
                        margin: 0;
                        font-family: 'Courier New', monospace;
                    }
                    .text {
                        color: #333;
                        font-size: 16px;
                        line-height: 1.6;
                        margin: 15px 0;
                    }
                    .warning {
                        background-color: #FEF3C7;
                        border-left: 4px solid #F59E0B;
                        padding: 15px;
                        margin: 20px 0;
                        border-radius: 4px;
                    }
                    .warning-text {
                        color: #92400E;
                        font-size: 14px;
                        margin: 0;
                    }
                    .footer {
                        background-color: #F9FAFB;
                        padding: 20px;
                        text-align: center;
                        font-size: 12px;
                        color: #6B7280;
                        border-top: 1px solid #E5E7EB;
                    }
                    .footer a {
                        color: #3B82F6;
                        text-decoration: none;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🏥 SemptomAI</h1>
                        <p style="margin: 10px 0 0 0; font-size: 14px;">Şifre Sıfırlama Doğrulama</p>
                    </div>
                    
                    <div class="content">
                        <p class="text">Merhaba,</p>
                        <p class="text">Şifrenizi sıfırlamak için aşağıdaki 6 haneli doğrulama kodunu kullanın:</p>
                        
                        <div class="code-box">
                            <p class="code">$code</p>
                        </div>
                        
                        <p class="text">Bu kodu uygulamamızda doğrulama ekranına girin.</p>
                        
                        <div class="warning">
                            <p class="warning-text">⏱️ <strong>Önemli:</strong> Bu kod 10 dakika geçerlidir.</p>
                            <p class="warning-text">🔒 Güvenlik nedeniyle bu kodu kimseyle paylaşmayın.</p>
                        </div>
                        
                        <p class="text" style="font-size: 14px; color: #6B7280;">
                            Eğer bu işlemi siz yapmadıysanız, bu e-postayı dikkate almayın ve hesabınızın güvenliğini kontrol edin.
                        </p>
                    </div>
                    
                    <div class="footer">
                        <p style="margin: 0 0 10px 0;">© 2024 SemptomAI. Tüm hakları saklıdır.</p>
                        <p style="margin: 0;">Bu otomatik bir e-postadır. Lütfen yanıtlamayın.</p>
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()
    }
}

