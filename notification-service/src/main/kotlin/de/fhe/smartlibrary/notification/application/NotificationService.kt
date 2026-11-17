package de.fhe.smartlibrary.notification.application

import de.fhe.smartlibrary.notification.domain.Notification
import de.fhe.smartlibrary.notification.domain.NotificationType
import jakarta.enterprise.context.ApplicationScoped
import java.time.LocalDateTime
import org.jboss.logging.Logger

@ApplicationScoped
class NotificationService {

    private val log: Logger = Logger.getLogger(NotificationService::class.java)

    fun sendLoanNotification(userId: Long, bookTitle: String, actionType: String) {
        val message = "Hallo! Das Buch '$bookTitle' wurde erfolgreich $actionType."

        val newNotification = Notification(
            id = null,
            userId = userId,
            message = message,
            type = NotificationType.EMAIL,
            createdAt = LocalDateTime.now()
        )

        log.info("Sending Notification to User $userId: ${newNotification.message}")
    }
}