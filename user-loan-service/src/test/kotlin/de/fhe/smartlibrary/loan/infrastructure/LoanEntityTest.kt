package de.fhe.smartlibrary.loan.infrastructure

import de.fhe.smartlibrary.loan.domain.Loan
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.vertx.RunOnVertxContext
import io.quarkus.test.hibernate.reactive.panache.TransactionalUniAsserter
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.time.LocalDateTime

@QuarkusTest
class LoanEntityTest {

    @Test
    fun `fromDomain creates entity with correct fields`() {
        val now  = LocalDateTime.of(2026, 6, 5, 12, 0)
        val loan = Loan(userId = 1L, bookId = 42L, loanDate = now, returnDate = null)
        val entity = LoanEntity.fromDomain(loan)
        assertEquals(1L, entity.userId)
        assertEquals(42L, entity.bookId)
        assertEquals(now, entity.loanDate)
        assertNull(entity.returnDate)
    }

    @Test
    fun `toDomain converts entity correctly`() {
        val now = LocalDateTime.of(2026, 6, 5, 12, 0)
        val entity = LoanEntity().apply {
            userId     = 10L
            bookId     = 5L
            loanDate   = now
            returnDate = null
        }
        val loan = entity.toDomain()
        assertEquals(10L, loan.userId)
        assertEquals(5L, loan.bookId)
        assertEquals(now, loan.loanDate)
        assertNull(loan.returnDate)
    }

    @Test
    fun `fromDomain with returnDate`() {
        val loanDate   = LocalDateTime.of(2026, 1, 1, 10, 0)
        val returnDate = LocalDateTime.of(2026, 1, 14, 10, 0)
        val loan   = Loan(userId = 1L, bookId = 1L, loanDate = loanDate, returnDate = returnDate)
        val entity = LoanEntity.fromDomain(loan)
        assertEquals(returnDate, entity.returnDate)
    }

    @Test
    fun `toDomain with returnDate`() {
        val returnDate = LocalDateTime.of(2026, 2, 1, 9, 0)
        val entity = LoanEntity().apply {
            userId     = 1L
            bookId     = 1L
            loanDate   = LocalDateTime.now()
            this.returnDate = returnDate
        }
        assertEquals(returnDate, entity.toDomain().returnDate)
    }

    @Test
    fun `roundtrip fromDomain toDomain preserves fields`() {
        val now  = LocalDateTime.of(2026, 3, 15, 8, 0)
        val loan = Loan(userId = 7L, bookId = 3L, loanDate = now, returnDate = null)
        val result = LoanEntity.fromDomain(loan).toDomain()
        assertEquals(loan.userId,  result.userId)
        assertEquals(loan.bookId,  result.bookId)
        assertEquals(loan.loanDate, result.loanDate)
        assertNull(result.returnDate)
    }

    @Test
    fun `entity default loanDate is set`() {
        val entity = LoanEntity()
        assertNotNull(entity.loanDate)
    }

    @Test
    @RunOnVertxContext
    fun `persist and find loan entity`(asserter: TransactionalUniAsserter) {
        val now    = LocalDateTime.of(2026, 6, 5, 12, 0)
        val entity = LoanEntity().apply {
            userId   = 99L
            bookId   = 88L
            loanDate = now
        }
        asserter.assertThat(
            { entity.persist<LoanEntity>() },
            { persisted ->
                assertNotNull(persisted.id)
                assertEquals(99L, persisted.userId)
                assertEquals(88L, persisted.bookId)
            }
        )
    }
}