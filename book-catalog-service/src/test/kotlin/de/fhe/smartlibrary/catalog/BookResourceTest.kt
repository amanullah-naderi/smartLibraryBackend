package de.fhe.smartlibrary.catalog

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.hamcrest.CoreMatchers.*
import org.hamcrest.Matchers.greaterThan
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder


@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class BookResourceTest {

    @Test
    @Order(1)
    fun `GET books returns empty list or list`() {
        given()
            .contentType(ContentType.JSON)
            .`when`()
            .get("/books")
            .then()
            .statusCode(200)
    }

    @Test
    @Order(2)
    fun `POST books creates a book and returns 201`() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                  "title": "Clean Code",
                  "author": "Robert C. Martin",
                  "isbn": "978-0132350884-test",
                  "available": true
                }
            """.trimIndent())
            .`when`()
            .post("/books")
            .then()
            .statusCode(201)
            .body("title",  equalTo("Clean Code"))
            .body("author", equalTo("Robert C. Martin"))
            .body("isbn",   equalTo("978-0132350884-test"))
            .body("available", equalTo(true))
            .body("id", notNullValue())
    }

    @Test
    @Order(3)
    fun `GET books after POST returns non-empty list`() {
        given()
            .contentType(ContentType.JSON)
            .`when`()
            .get("/books")
            .then()
            .statusCode(200)
            .body("size()", greaterThan(0))
    }

    @Test
    @Order(4)
    fun `GET books by id returns book`() {
        val id = given()
            .contentType(ContentType.JSON)
            .body("""
                {
                  "title": "Kotlin in Action",
                  "author": "JetBrains",
                  "isbn": "978-1617293290-test",
                  "available": true
                }
            """.trimIndent())
            .`when`()
            .post("/books")
            .then()
            .statusCode(201)
            .extract().path<Int>("id")

        given()
            .contentType(ContentType.JSON)
            .`when`()
            .get("/books/$id")
            .then()
            .statusCode(200)
            .body("id",    equalTo(id))
            .body("title", equalTo("Kotlin in Action"))
    }

    @Test
    @Order(5)
    fun `GET books by non-existing id returns 404`() {
        given()
            .contentType(ContentType.JSON)
            .`when`()
            .get("/books/999999")
            .then()
            .statusCode(404)
    }

    @Test
    @Order(6)
    fun `PATCH books availability sets available to false`() {
        val id = given()
            .contentType(ContentType.JSON)
            .body("""
                {
                  "title": "Effective Java",
                  "author": "Joshua Bloch",
                  "isbn": "978-0134685991-test",
                  "available": true
                }
            """.trimIndent())
            .`when`()
            .post("/books")
            .then()
            .statusCode(201)
            .extract().path<Int>("id")

        given()
            .contentType(ContentType.JSON)
            .queryParam("available", false)
            .`when`()
            .patch("/books/$id/availability")
            .then()
            .statusCode(200)
            .body("available", equalTo(false))
    }

    @Test
    @Order(7)
    fun `PATCH books availability non-existing id returns 404`() {
        given()
            .contentType(ContentType.JSON)
            .queryParam("available", false)
            .`when`()
            .patch("/books/999999/availability")
            .then()
            .statusCode(404)
    }

    @Test
    @Order(8)
    fun `DELETE books removes book`() {
        val id = given()
            .contentType(ContentType.JSON)
            .body("""
                {
                  "title": "Delete Me",
                  "author": "Test Author",
                  "isbn": "978-0000000000-test",
                  "available": true
                }
            """.trimIndent())
            .`when`()
            .post("/books")
            .then()
            .statusCode(201)
            .extract().path<Int>("id")

        given()
            .`when`()
            .delete("/books/$id")
            .then()
            .statusCode(204)

        given()
            .`when`()
            .get("/books/$id")
            .then()
            .statusCode(404)
    }

    @Test
    @Order(9)
    fun `DELETE books non-existing id returns 404`() {
        given()
            .`when`()
            .delete("/books/999999")
            .then()
            .statusCode(404)
    }
}