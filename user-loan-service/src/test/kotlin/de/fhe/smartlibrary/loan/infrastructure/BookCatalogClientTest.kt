package de.fhe.smartlibrary.loan.infrastructure

import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

@QuarkusTest
class BookCatalogClientTest {

    @Test
    fun `BookDto default values are correct`() {
        val dto = BookDto()
        assertNull(dto.id)
        assertEquals("", dto.title)
        assertEquals("", dto.author)
        assertEquals("", dto.isbn)
        assertTrue(dto.available)
    }

    @Test
    fun `BookDto can be created with all fields`() {
        val dto = BookDto(
            id        = 1L,
            title     = "Clean Code",
            author    = "Robert C. Martin",
            isbn      = "978-0132350884",
            available = true
        )
        assertEquals(1L, dto.id)
        assertEquals("Clean Code", dto.title)
        assertEquals("Robert C. Martin", dto.author)
        assertEquals("978-0132350884", dto.isbn)
        assertTrue(dto.available)
    }

    @Test
    fun `BookDto available can be false`() {
        val dto = BookDto(id = 2L, title = "T", author = "A", isbn = "I", available = false)
        assertFalse(dto.available)
    }

    @Test
    fun `BookDto equality works`() {
        val d1 = BookDto(1L, "Clean Code", "Martin", "978-test", true)
        val d2 = BookDto(1L, "Clean Code", "Martin", "978-test", true)
        assertEquals(d1, d2)
        assertEquals(d1.hashCode(), d2.hashCode())
    }

    @Test
    fun `BookDto copy works`() {
        val original = BookDto(1L, "Original", "Author", "ISBN", true)
        val updated  = original.copy(available = false)
        assertFalse(updated.available)
        assertEquals(original.title, updated.title)
    }

    @Test
    fun `BookDto id can be null`() {
        val dto = BookDto(id = null, title = "T", author = "A", isbn = "I", available = true)
        assertNull(dto.id)
    }
}