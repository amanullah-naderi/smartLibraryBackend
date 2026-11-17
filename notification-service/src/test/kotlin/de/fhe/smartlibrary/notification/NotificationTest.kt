package de.fhe.smartlibrary.notification

import de.fhe.smartlibrary.notification.domain.Notification
import de.fhe.smartlibrary.notification.domain.NotificationType
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.time.LocalDateTime

@QuarkusTest
class NotificationTest {

    @Test
    fun `Notification can be created with all fields`() {
        val notification = Notification(
            id        = "notif-1",
            userId    = 42L,
            message   = "Buch ausgeliehen",
            type      = NotificationType.EMAIL,
            createdAt = LocalDateTime.now()
        )
        assertEquals(NotificationType.EMAIL, notification.type)
        assertEquals(42L, notification.userId)
        assertEquals("Buch ausgeliehen", notification.message)
        assertEquals("notif-1", notification.id)
    }

    @Test
    fun `Notification supports all NotificationTypes`() {
        NotificationType.entries.forEach { type ->
            val n = Notification(
                id        = null,
                userId    = 1L,
                message   = "test",
                type      = type,
                createdAt = LocalDateTime.now()
            )
            assertEquals(type, n.type)
        }
    }

    @Test
    fun `NotificationType values are correct`() {
        val names = NotificationType.entries.map { it.name }
        assertTrue(names.contains("EMAIL"))
        assertTrue(names.contains("PUSH"))
        assertTrue(names.contains("SMS"))
    }

    @Test
    fun `Notification equality works`() {
        val now = LocalDateTime.of(2026, 6, 5, 12, 0)
        val n1 = Notification(id = "1", userId = 2L, message = "msg", type = NotificationType.EMAIL, createdAt = now)
        val n2 = Notification(id = "1", userId = 2L, message = "msg", type = NotificationType.EMAIL, createdAt = now)
        assertEquals(n1, n2)
    }
}