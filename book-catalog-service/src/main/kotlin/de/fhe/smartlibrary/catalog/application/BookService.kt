package de.fhe.smartlibrary.catalog.application

import de.fhe.smartlibrary.catalog.domain.Book
import de.fhe.smartlibrary.catalog.infrastructure.BookEntity
import io.quarkus.hibernate.reactive.panache.Panache
import io.smallrye.mutiny.Uni
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class BookService {

    fun getAllBooks(): Uni<List<Book>> =
        Panache.withSession {
            BookEntity.listAll().map { list -> list.map { it.toDomain() } }
        }

    fun getBookById(id: Long): Uni<Book?> =
        Panache.withSession {
            BookEntity.findById(id).map { it?.toDomain() }
        }

    fun createBook(book: Book): Uni<Book> =
        Panache.withTransaction {
            BookEntity.fromDomain(book)
                .persist<BookEntity>()
                .map { it.toDomain() }
        }

    fun updateAvailability(id: Long, available: Boolean): Uni<Book?> =
        Panache.withTransaction {
            BookEntity.findById(id).flatMap { entity ->
                if (entity == null) Uni.createFrom().nullItem()
                else {
                    entity.available = available
                    entity.persist<BookEntity>().map { it.toDomain() }
                }
            }
        }

    fun deleteBook(id: Long): Uni<Boolean> =
        Panache.withTransaction {
            BookEntity.deleteById(id)
        }
}