package de.fhe.smartlibrary.loan

import de.fhe.smartlibrary.loan.infrastructure.BookCatalogClient
import de.fhe.smartlibrary.loan.infrastructure.BookDto
import io.quarkus.test.InjectMock
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import io.smallrye.mutiny.Uni
import org.eclipse.microprofile.rest.client.inject.RestClient
import org.hamcrest.CoreMatchers.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever

@QuarkusTest
class LoanResourceTest {

    @InjectMock
    @RestClient
    lateinit var bookCatalogClient: BookCatalogClient

    @Test
    fun `getAllLoans returns 200 OK`() {
        given()
            .`when`().get("/loans")
            .then()
            .statusCode(200)
            .body("isEmpty()", `is`(false))
    }

    @Test
    fun `createLoan returns 201 CREATED with loan data`() {
        val bookDto = BookDto(id = 10L, title = "Clean Code", available = true)
        whenever(bookCatalogClient.getBook(10L)).thenReturn(Uni.createFrom().item(bookDto))
        whenever(bookCatalogClient.updateAvailability(10L, false)).thenReturn(Uni.createFrom().item(bookDto.copy(available = false)))

        val loanJson = """
            {
                "userId": 10,
                "bookId": 10,
                "loanDate": "2025-01-01T10:00:00"
            }
        """.trimIndent()

        given()
            .contentType(ContentType.JSON)
            .body(loanJson)
            .`when`().post("/loans")
            .then()
            .statusCode(201)
            .body("userId", equalTo(10))
            .body("bookId", equalTo(10))
            .body("id", notNullValue())
    }

    @Test
    fun `getLoanById returns 404 for non-existing loan`() {
        given()
            .`when`().get("/loans/99999")
            .then()
            .statusCode(404)
    }

    @Test
    fun `returnLoan returns 200 OK with updated loan`() {
        val bookDto = BookDto(id = 11L, title = "Clean Code", available = true)
        whenever(bookCatalogClient.getBook(11L)).thenReturn(Uni.createFrom().item(bookDto))
        whenever(bookCatalogClient.updateAvailability(11L, false)).thenReturn(Uni.createFrom().item(bookDto.copy(available = false)))
        whenever(bookCatalogClient.updateAvailability(11L, true)).thenReturn(Uni.createFrom().item(bookDto.copy(available = true)))

        val loanJson = """
            {
                "userId": 11,
                "bookId": 11,
                "loanDate": "2025-01-01T10:00:00"
            }
        """.trimIndent()

        val createResponse = given()
            .contentType(ContentType.JSON)
            .body(loanJson)
            .`when`().post("/loans")
            .then()
            .statusCode(201)
            .extract().path<Number>("id").toLong()

        given()
            .`when`().patch("/loans/$createResponse/return")
            .then()
            .statusCode(200)
            .body("returnDate", notNullValue())
    }

    @Test
    fun `getActiveLoansByUser returns list`() {
        given()
            .`when`().get("/loans/user/10")
            .then()
            .statusCode(200)
            .body("isEmpty()", anything())
    }

    @Test
    fun `getActiveLoansByBook returns list`() {
        given()
            .`when`().get("/loans/book/10")
            .then()
            .statusCode(200)
            .body("isEmpty()", anything())
    }

    @Test
    fun `deleteLoan returns 204 NO CONTENT`() {
        val bookDto = BookDto(id = 12L, title = "Clean Code", available = true)
        whenever(bookCatalogClient.getBook(12L)).thenReturn(Uni.createFrom().item(bookDto))
        whenever(bookCatalogClient.updateAvailability(12L, false)).thenReturn(Uni.createFrom().item(bookDto.copy(available = false)))

        val loanJson = """
            {
                "userId": 12,
                "bookId": 12,
                "loanDate": "2025-01-01T10:00:00"
            }
        """.trimIndent()

        val createResponse = given()
            .contentType(ContentType.JSON)
            .body(loanJson)
            .`when`().post("/loans")
            .then()
            .statusCode(201)
            .extract().path<Number>("id").toLong()

        given()
            .`when`().delete("/loans/$createResponse")
            .then()
            .statusCode(204)
    }
}
