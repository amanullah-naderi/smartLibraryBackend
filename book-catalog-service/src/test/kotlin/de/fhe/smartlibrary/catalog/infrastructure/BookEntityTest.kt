package de.fhe.smartlibrary.catalog.infrastructure

import de.fhe.smartlibrary.catalog.domain.Book
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.vertx.RunOnVertxContext
import io.quarkus.test.hibernate.reactive.panache.TransactionalUniAsserter
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

@QuarkusTest
class BookEntityTest {

    @Test
    fun `fromDomain creates entity with correct fields`() {
        val book = Book(title = "Clean Code", author = "Robert C. Martin", isbn = "978-test-1", available = true)
        val entity = BookEntity.fromDomain(book)
        assertEquals("Clean Code", entity.title)
        assertEquals("Robert C. Martin", entity.author)
        assertEquals("978-test-1", entity.isbn)
        assertTrue(entity.available)
    }

    @Test
    fun `toDomain converts entity fields correctly`() {
        val entity = BookEntity().apply {
            title     = "Kotlin in Action"
            author    = "JetBrains"
            isbn      = "978-test-2"
            available = false
        }
        val book = entity.toDomain()
        assertEquals("Kotlin in Action", book.title)
        assertEquals("JetBrains", book.author)
        assertFalse(book.available)
    }

    @Test
    fun `fromDomain respects available false`() {
        val book = Book(title = "T", author = "A", isbn = "I-test", available = false)
        assertFalse(BookEntity.fromDomain(book).available)
    }

    @Test
    fun `entity available defaults to true`() {
        assertTrue(BookEntity().available)
    }

    @Test
    fun `roundtrip fromDomain toDomain preserves all fields`() {
        val original = Book(title = "Test", author = "Author", isbn = "999-test", available = true)
        val result = BookEntity.fromDomain(original).toDomain()
        assertEquals(original.title, result.title)
        assertEquals(original.author, result.author)
        assertEquals(original.isbn, result.isbn)
        assertEquals(original.available, result.available)
    }

    @Test
    @RunOnVertxContext
    fun `persist and find book entity`(asserter: TransactionalUniAsserter) {
        val book = Book(title = "Persisted Book", author = "Author", isbn = "persist-test-${System.currentTimeMillis()}", available = true)
        val entity = BookEntity.fromDomain(book)

        asserter.assertThat(
            { entity.persist<BookEntity>() },
            { persisted ->
                assertNotNull(persisted.id)
                assertEquals("Persisted Book", persisted.title)
            }
        )
    }
}