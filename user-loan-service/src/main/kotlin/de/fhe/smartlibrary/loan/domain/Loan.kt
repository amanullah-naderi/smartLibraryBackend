package de.fhe.smartlibrary.loan.domain

import java.time.LocalDateTime

data class Loan(
    val id: Long? = null,
    val userId: Long = 0,
    val bookId: Long = 0,
    val loanDate: LocalDateTime = LocalDateTime.now(),
    val returnDate: LocalDateTime? = null
)
