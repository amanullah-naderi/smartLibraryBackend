package de.fhe.smartlibrary.notification.domain

import java.time.LocalDateTime

data class Notification(
    val id: String?,
    val userId: Long,
    val message: String,
    val type: NotificationType,
    val createdAt: LocalDateTime
)

enum class NotificationType {
    EMAIL, PUSH, SMS
}