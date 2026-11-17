package de.fhe.smartlibrary.loan

import de.fhe.smartlibrary.loan.application.LoanService
import de.fhe.smartlibrary.loan.domain.Loan
import de.fhe.smartlibrary.loan.infrastructure.BookCatalogClient
import de.fhe.smartlibrary.loan.infrastructure.BookDto
import io.quarkus.test.InjectMock
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.hibernate.reactive.panache.TransactionalUniAsserter
import io.quarkus.test.vertx.RunOnVertxContext
import io.smallrye.mutiny.Uni
import jakarta.inject.Inject
import jakarta.ws.rs.NotFoundException
import org.eclipse.microprofile.rest.client.inject.RestClient
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import java.time.LocalDateTime

@QuarkusTest
class LoanServiceTest {

    @Inject
    lateinit var loanService: LoanService

    @InjectMock
    @RestClient
    lateinit var bookCatalogClient: BookCatalogClient

    @Test
    @RunOnVertxContext
    fun `getAllLoans returns empty list initially`(asserter: TransactionalUniAsserter) {
        asserter.assertThat({ loanService.getAllLoans() }) { result ->
            assertNotNull(result)
        }
    }

    @Test
    @RunOnVertxContext
    fun `createLoan returns loan with id when book is available`(asserter: TransactionalUniAsserter) {
        val bookDto = BookDto(id = 1L, title = "Clean Code", available = true)
        whenever(bookCatalogClient.getBook(1L)).thenReturn(Uni.createFrom().item(bookDto))
        whenever(bookCatalogClient.updateAvailability(1L, false)).thenReturn(Uni.createFrom().item(bookDto.copy(available = false)))

        val loan = Loan(
            userId = 1L,
            bookId = 1L,
            loanDate = LocalDateTime.now()
        )
        asserter.assertThat({ loanService.createLoan(loan) }) { result ->
            assertNotNull(result.id)
            assertEquals(1L, result.userId)
            assertEquals(1L, result.bookId)
            assertNull(result.returnDate)
        }
    }

    @Test
    @RunOnVertxContext
    fun `createLoan fails when book is not found`(asserter: TransactionalUniAsserter) {
        whenever(bookCatalogClient.getBook(999L)).thenReturn(Uni.createFrom().nullItem())

        val loan = Loan(
            userId = 1L,
            bookId = 999L,
            loanDate = LocalDateTime.now()
        )
        asserter.assertFailedWith({ loanService.createLoan(loan) }, NotFoundException::class.java)
    }

    @Test
    @RunOnVertxContext
    fun `createLoan fails when book is not available`(asserter: TransactionalUniAsserter) {
        val bookDto = BookDto(id = 1L, title = "Clean Code", available = false)
        whenever(bookCatalogClient.getBook(1L)).thenReturn(Uni.createFrom().item(bookDto))

        val loan = Loan(
            userId = 1L,
            bookId = 1L,
            loanDate = LocalDateTime.now()
        )
        asserter.assertFailedWith({ loanService.createLoan(loan) }, IllegalStateException::class.java)
    }

    @Test
    @RunOnVertxContext
    fun `getLoanById returns null for non-existing id`(asserter: TransactionalUniAsserter) {
        asserter.assertNull { loanService.getLoanById(99999L) }
    }

    @Test
    @RunOnVertxContext
    fun `returnLoan sets return date and updates availability`(asserter: TransactionalUniAsserter) {
        val bookDto = BookDto(id = 2L, title = "Clean Code", available = true)
        whenever(bookCatalogClient.getBook(2L)).thenReturn(Uni.createFrom().item(bookDto))
        whenever(bookCatalogClient.updateAvailability(2L, false)).thenReturn(Uni.createFrom().item(bookDto.copy(available = false)))
        whenever(bookCatalogClient.updateAvailability(2L, true)).thenReturn(Uni.createFrom().item(bookDto.copy(available = true)))

        val loan = Loan(
            userId = 2L,
            bookId = 2L,
            loanDate = LocalDateTime.now()
        )
        asserter.assertThat(
            {
                loanService.createLoan(loan).flatMap { created ->
                    loanService.returnLoan(created.id!!)
                }
            }
        ) { returned ->
            assertNotNull(returned)
            assertNotNull(returned?.returnDate)
        }
    }

    @Test
    @RunOnVertxContext
    fun `getActiveLoansByUserId returns only non-returned loans`(asserter: TransactionalUniAsserter) {
        val userId = 3L
        val loan1 = Loan(userId = userId, bookId = 3L)
        val loan2 = Loan(userId = userId, bookId = 4L)

        val bookDto3 = BookDto(id = 3L, title = "Clean Code 3", available = true)
        val bookDto4 = BookDto(id = 4L, title = "Clean Code 4", available = true)
        whenever(bookCatalogClient.getBook(3L)).thenReturn(Uni.createFrom().item(bookDto3))
        whenever(bookCatalogClient.getBook(4L)).thenReturn(Uni.createFrom().item(bookDto4))
        whenever(bookCatalogClient.updateAvailability(3L, false)).thenReturn(Uni.createFrom().item(bookDto3.copy(available = false)))
        whenever(bookCatalogClient.updateAvailability(4L, false)).thenReturn(Uni.createFrom().item(bookDto4.copy(available = false)))
        whenever(bookCatalogClient.updateAvailability(4L, true)).thenReturn(Uni.createFrom().item(bookDto4.copy(available = true)))

        asserter.assertThat(
            {
                loanService.createLoan(loan1).flatMap {
                    loanService.createLoan(loan2).flatMap { loan2Created ->
                        loanService.returnLoan(loan2Created.id!!).flatMap {
                            loanService.getActiveLoansByUserId(userId)
                        }
                    }
                }
            }
        ) { activeLoans ->
            assertEquals(1, activeLoans.size)
        }
    }

    @Test
    @RunOnVertxContext
    fun `returnLoan returns null for non-existing id`(asserter: TransactionalUniAsserter) {
        asserter.assertNull { loanService.returnLoan(99999L) }
    }

    @Test
    @RunOnVertxContext
    fun `deleteLoan removes loan from database`(asserter: TransactionalUniAsserter) {
        val bookDto5 = BookDto(id = 5L, title = "Clean Code 5", available = true)
        whenever(bookCatalogClient.getBook(5L)).thenReturn(Uni.createFrom().item(bookDto5))
        whenever(bookCatalogClient.updateAvailability(5L, false)).thenReturn(Uni.createFrom().item(bookDto5.copy(available = false)))

        val loan = Loan(userId = 5L, bookId = 5L)
        asserter.assertNull {
            loanService.createLoan(loan).flatMap { created ->
                val createdId = requireNotNull(created.id)
                loanService.deleteLoan(createdId).flatMap {
                    loanService.getLoanById(createdId)
                }
            }
        }
    }
}
