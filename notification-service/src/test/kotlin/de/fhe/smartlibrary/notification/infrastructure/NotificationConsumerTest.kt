package de.fhe.smartlibrary.notification.infrastructure

import io.quarkus.test.InjectMock
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.verify
import de.fhe.smartlibrary.notification.application.NotificationService

@QuarkusTest
class NotificationConsumerTest {

    @Inject
    lateinit var consumer: NotificationConsumer

    @InjectMock
    lateinit var notificationService: NotificationService

    @Test
    fun `consumeLoanEvent does not throw on valid payload`() {
        doNothing().`when`(notificationService).sendLoanNotification(any(), any(), any())
        assertDoesNotThrow {
            consumer.consumeLoanEvent("""{"bookId":1,"userId":10}""")
        }
    }

    @Test
    fun `consumeLoanEvent calls notificationService`() {
        doNothing().`when`(notificationService).sendLoanNotification(any(), any(), any())
        consumer.consumeLoanEvent("""{"bookId":1,"userId":10}""")
        verify(notificationService).sendLoanNotification(any(), any(), any())
    }

    @Test
    fun `consumeLoanEvent handles empty string`() {
        doNothing().`when`(notificationService).sendLoanNotification(any(), any(), any())
        assertDoesNotThrow {
            consumer.consumeLoanEvent("")
        }
    }

    @Test
    fun `consumeLoanEvent handles malformed json`() {
        doNothing().`when`(notificationService).sendLoanNotification(any(), any(), any())
        assertDoesNotThrow {
            consumer.consumeLoanEvent("not-json")
        }
    }
}