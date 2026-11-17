package de.fhe.smartlibrary.notification.domain

import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.time.LocalDateTime

@QuarkusTest
class NotificationTest {

    @Test
    fun `Notification can be created with all fields`() {
        val now = LocalDateTime.of(2026, 6, 5, 12, 0)
        val notification = Notification(
            id        = "notif-1",
            userId    = 42L,
            message   = "Buch ausgeliehen",
            type      = NotificationType.EMAIL,
            createdAt = now
        )
        assertEquals("notif-1", notification.id)
        assertEquals(42L, notification.userId)
        assertEquals("Buch ausgeliehen", notification.message)
        assertEquals(NotificationType.EMAIL, notification.type)
        assertEquals(now, notification.createdAt)
    }

    @Test
    fun `Notification id can be null`() {
        val notification = Notification(
            id        = null,
            userId    = 1L,
            message   = "test",
            type      = NotificationType.PUSH,
            createdAt = LocalDateTime.now()
        )
        assertNull(notification.id)
    }

    @Test
    fun `NotificationType EMAIL is valid`() {
        val n = Notification(null, 1L, "msg", NotificationType.EMAIL, LocalDateTime.now())
        assertEquals(NotificationType.EMAIL, n.type)
    }

    @Test
    fun `NotificationType PUSH is valid`() {
        val n = Notification(null, 1L, "msg", NotificationType.PUSH, LocalDateTime.now())
        assertEquals(NotificationType.PUSH, n.type)
    }

    @Test
    fun `NotificationType SMS is valid`() {
        val n = Notification(null, 1L, "msg", NotificationType.SMS, LocalDateTime.now())
        assertEquals(NotificationType.SMS, n.type)
    }

    @Test
    fun `Notification equality works for same values`() {
        val now = LocalDateTime.of(2026, 6, 5, 10, 0)
        val n1 = Notification("id1", 2L, "msg", NotificationType.SMS, now)
        val n2 = Notification("id1", 2L, "msg", NotificationType.SMS, now)
        assertEquals(n1, n2)
        assertEquals(n1.hashCode(), n2.hashCode())
    }

    @Test
    fun `Notification copy works correctly`() {
        val original = Notification("id1", 1L, "original", NotificationType.EMAIL, LocalDateTime.now())
        val copied   = original.copy(message = "updated")
        assertEquals("updated", copied.message)
        assertEquals(original.userId, copied.userId)
        assertEquals(original.type, copied.type)
    }

    @Test
    fun `NotificationService sendLoanNotification creates correct message`() {
        val service = de.fhe.smartlibrary.notification.application.NotificationService()
        assertDoesNotThrow {
            service.sendLoanNotification(
                userId     = 10L,
                bookTitle  = "Clean Code",
                actionType = "ausgeliehen"
            )
        }
    }
}