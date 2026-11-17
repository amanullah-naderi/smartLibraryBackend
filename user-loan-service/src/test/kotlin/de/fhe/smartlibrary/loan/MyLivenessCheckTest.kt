package de.fhe.smartlibrary.loan

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import org.hamcrest.CoreMatchers.equalTo
import org.junit.jupiter.api.Test

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
        val check = de.fhe.smartlibrary.MyLivenessCheck()
        val response = check.call()
        assert(response.status == org.eclipse.microprofile.health.HealthCheckResponse.Status.UP)
        assert(response.name == "alive")
    }
}