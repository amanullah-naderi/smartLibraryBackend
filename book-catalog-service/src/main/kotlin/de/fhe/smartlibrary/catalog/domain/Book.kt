package de.fhe.smartlibrary.catalog.domain

data class Book(
    val id: Long? = null,
    val title: String = "",
    val author: String = "",
    val isbn: String = "",
    val available: Boolean = true
)