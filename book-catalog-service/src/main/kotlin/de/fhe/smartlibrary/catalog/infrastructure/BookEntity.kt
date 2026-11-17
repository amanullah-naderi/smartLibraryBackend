package de.fhe.smartlibrary.catalog.infrastructure

import de.fhe.smartlibrary.catalog.domain.Book
import io.quarkus.hibernate.reactive.panache.kotlin.PanacheCompanion
import io.quarkus.hibernate.reactive.panache.kotlin.PanacheEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "books")
class BookEntity : PanacheEntity() {

    @Column(nullable = false)
    var title: String = ""

    @Column(nullable = false)
    var author: String = ""

    @Column(nullable = false, unique = true)
    var isbn: String = ""

    @Column(nullable = false)
    var available: Boolean = true

    fun toDomain() = Book(
        id        = this.id,
        title     = this.title,
        author    = this.author,
        isbn      = this.isbn,
        available = this.available
    )

    companion object : PanacheCompanion<BookEntity> {
        fun fromDomain(book: Book) = BookEntity().apply {
            title     = book.title
            author    = book.author
            isbn      = book.isbn
            available = book.available
        }
    }
}