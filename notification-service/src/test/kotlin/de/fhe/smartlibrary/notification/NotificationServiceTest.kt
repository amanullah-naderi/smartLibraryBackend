package de.fhe.smartlibrary.notification

import de.fhe.smartlibrary.notification.application.NotificationService
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

@QuarkusTest
class NotificationServiceTest {

    @Inject
    lateinit var notificationService: NotificationService

    @Test
    fun `testSendLoanNotificationShouldNotThrowException`() {

        assertDoesNotThrow {
            notificationService.sendLoanNotification(
                userId = 10L,
                bookTitle = "Clean Code",
                actionType = "ausgeliehen"
            )
        }
    }
}