package de.fhe.smartlibrary.catalog.api

import de.fhe.smartlibrary.catalog.application.BookService
import de.fhe.smartlibrary.catalog.domain.Book
import io.smallrye.mutiny.Uni
import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response

@Path("/books")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class BookResource {

    @Inject
    lateinit var bookService: BookService

    @GET
    fun getAllBooks(): Uni<List<Book>> =
        bookService.getAllBooks()

    @GET
    @Path("/{id}")
    fun getBook(@PathParam("id") id: Long): Uni<Response> =
        bookService.getBookById(id).map { book ->
            if (book != null) Response.ok(book).build()
            else Response.status(Response.Status.NOT_FOUND).build()
        }

    @POST
    fun createBook(book: Book): Uni<Response> =
        bookService.createBook(book).map {
            Response.status(Response.Status.CREATED).entity(it).build()
        }

    @PATCH
    @Path("/{id}/availability")
    fun updateAvailability(
        @PathParam("id") id: Long,
        @QueryParam("available") available: Boolean
    ): Uni<Response> =
        bookService.updateAvailability(id, available).map { book ->
            if (book != null) Response.ok(book).build()
            else Response.status(Response.Status.NOT_FOUND).build()
        }

    @DELETE
    @Path("/{id}")
    fun deleteBook(@PathParam("id") id: Long): Uni<Response> =
        bookService.deleteBook(id).map { deleted ->
            if (deleted) Response.noContent().build()
            else Response.status(Response.Status.NOT_FOUND).build()
        }
}