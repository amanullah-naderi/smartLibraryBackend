package de.fhe.smartlibrary.catalog

import de.fhe.smartlibrary.catalog.application.BookService
import de.fhe.smartlibrary.catalog.domain.Book
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.hibernate.reactive.panache.TransactionalUniAsserter
import io.quarkus.test.vertx.RunOnVertxContext
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

@QuarkusTest
class BookServiceTest {

    @Inject
    lateinit var bookService: BookService

    @Test
    @RunOnVertxContext
    fun `getAllBooks returns empty list initially`(asserter: TransactionalUniAsserter) {
        asserter.assertThat({ bookService.getAllBooks() }) { result ->
            assertNotNull(result)
            assertTrue(result is List)
        }
    }

    @Test
    @RunOnVertxContext
    fun `createBook returns book with id`(asserter: TransactionalUniAsserter) {
        val uniqueIsbn = "978-1617293290-${System.currentTimeMillis()}"
        val book = Book(
            title  = "Kotlin in Action",
            author = "JetBrains",
            isbn   = uniqueIsbn
        )
        asserter.assertThat({ bookService.createBook(book) }) { result ->
            assertNotNull(result.id)
            assertEquals("Kotlin in Action", result.title)
            assertEquals("JetBrains", result.author)
        }
    }

    @Test
    @RunOnVertxContext
    fun `getBookById returns null for non-existing id`(asserter: TransactionalUniAsserter) {
        asserter.assertNull { bookService.getBookById(99999L) }
    }

    @Test
    @RunOnVertxContext
    fun `updateAvailability returns null for non-existing id`(asserter: TransactionalUniAsserter) {
        asserter.assertNull { bookService.updateAvailability(99999L, false) }
    }
}