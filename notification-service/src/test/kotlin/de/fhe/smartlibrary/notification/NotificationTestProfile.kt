package de.fhe.smartlibrary.notification

import io.quarkus.test.junit.QuarkusTestProfile

class NotificationTestProfile : QuarkusTestProfile {
    override fun getConfigOverrides(): Map<String, String> = mapOf(
        "mp.messaging.incoming.loan-events.connector" to "smallrye-in-memory"
    )
}