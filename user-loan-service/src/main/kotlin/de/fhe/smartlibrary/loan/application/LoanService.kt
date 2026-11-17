package de.fhe.smartlibrary.loan.application

import de.fhe.smartlibrary.loan.domain.Loan
import de.fhe.smartlibrary.loan.infrastructure.BookCatalogClient
import de.fhe.smartlibrary.loan.infrastructure.LoanEntity
import io.quarkus.hibernate.reactive.panache.Panache
import io.smallrye.mutiny.Uni
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.ws.rs.NotFoundException
import org.eclipse.microprofile.rest.client.inject.RestClient
import java.time.LocalDateTime

@ApplicationScoped
class LoanService {

    @Inject
    @RestClient
    lateinit var bookCatalogClient: BookCatalogClient

    fun getAllLoans(): Uni<List<Loan>> =
        Panache.withSession {
            LoanEntity.listAll().map { list -> list.map { it.toDomain() } }
        }

    fun getLoanById(id: Long): Uni<Loan?> =
        Panache.withSession {
            LoanEntity.findById(id).map { it?.toDomain() }
        }

    fun getActiveLoansByUserId(userId: Long): Uni<List<Loan>> =
        Panache.withSession {
            LoanEntity.find("userId = ?1 and returnDate is null", userId)
                .list()
                .map { list -> list.map { it.toDomain() } }
        }

    fun getActiveLoansByBookId(bookId: Long): Uni<List<Loan>> =
        Panache.withSession {
            LoanEntity.find("bookId = ?1 and returnDate is null", bookId)
                .list()
                .map { list -> list.map { it.toDomain() } }
        }

    fun createLoan(loan: Loan): Uni<Loan> =
        bookCatalogClient.getBook(loan.bookId)
            .onFailure().recoverWithNull()
            .flatMap { book ->
                if (book == null) {
                    Uni.createFrom().failure(NotFoundException("Book not found"))
                } else if (!book.available) {
                    Uni.createFrom().failure(IllegalStateException("Book is not available"))
                } else {
                    bookCatalogClient.updateAvailability(loan.bookId, false)
                        .flatMap {
                            Panache.withTransaction {
                                LoanEntity.fromDomain(loan)
                                    .persist<LoanEntity>()
                                    .map { it.toDomain() }
                            }
                        }
                }
            }

    fun returnLoan(id: Long): Uni<Loan?> =
        Panache.withTransaction {
            LoanEntity.findById(id).flatMap { entity ->
                if (entity == null) Uni.createFrom().nullItem()
                else {
                    entity.returnDate = LocalDateTime.now()
                    entity.persist<LoanEntity>().flatMap { savedEntity ->
                        bookCatalogClient.updateAvailability(savedEntity.bookId, true)
                            .map { savedEntity.toDomain() }
                    }
                }
            }
        }

    fun deleteLoan(id: Long): Uni<Boolean> =
        Panache.withTransaction {
            LoanEntity.deleteById(id)
        }
}
