package de.fhe.smartlibrary

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import org.eclipse.microprofile.health.HealthCheckResponse
import org.hamcrest.CoreMatchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

@QuarkusTest
class MyLivenessCheckTest {

    @Test
    fun `health liveness endpoint returns UP`() {
        given()
            .`when`()
            .get("/q/health/live")
            .then()
            .statusCode(200)
            .body("status", equalTo("UP"))
    }

    @Test
    fun `MyLivenessCheck returns UP directly`() {
        val check = MyLivenessCheck()
        val response = check.call()
        assertEquals(HealthCheckResponse.Status.UP, response.status)
        assertEquals("alive", response.name)
    }
}