package de.fhe.smartlibrary.loan.infrastructure

import de.fhe.smartlibrary.loan.domain.Loan
import io.quarkus.hibernate.reactive.panache.kotlin.PanacheCompanion
import io.quarkus.hibernate.reactive.panache.kotlin.PanacheEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "loans")
class LoanEntity : PanacheEntity() {

    @Column(nullable = false)
    var userId: Long = 0

    @Column(nullable = false)
    var bookId: Long = 0

    @Column(nullable = false)
    var loanDate: LocalDateTime = LocalDateTime.now()

    @Column(nullable = true)
    var returnDate: LocalDateTime? = null

    fun toDomain() = Loan(
        id = this.id,
        userId = this.userId,
        bookId = this.bookId,
        loanDate = this.loanDate,
        returnDate = this.returnDate
    )

    companion object : PanacheCompanion<LoanEntity> {
        fun fromDomain(loan: Loan) = LoanEntity().apply {
            userId = loan.userId
            bookId = loan.bookId
            loanDate = loan.loanDate
            returnDate = loan.returnDate
        }
    }
}
