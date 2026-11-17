package de.fhe.smartlibrary.notification.infrastructure

import de.fhe.smartlibrary.notification.application.NotificationService
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.eclipse.microprofile.reactive.messaging.Incoming
import io.opentelemetry.instrumentation.annotations.WithSpan
import org.jboss.logging.Logger

@ApplicationScoped
class NotificationConsumer {

    private val log: Logger = Logger.getLogger(NotificationConsumer::class.java)

    @Inject
    private lateinit var notificationService: NotificationService

    @Incoming("loan-events")
    @WithSpan("Consume Loan Event")
    fun consumeLoanEvent(event: String) {
        log.info("Kafka Event empfangen: $event")

        try {
            notificationService.sendLoanNotification(
                userId = 10L,
                bookTitle = "Clean Code",
                actionType = "ausgeliehen"
            )
        } catch (e: Exception) {
            log.error("Fehler beim Verarbeiten des Kafka-Events", e)
        }
    }
}