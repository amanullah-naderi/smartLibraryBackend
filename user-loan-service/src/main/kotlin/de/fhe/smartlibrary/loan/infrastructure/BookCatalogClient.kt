package de.fhe.smartlibrary.loan.infrastructure

import io.smallrye.mutiny.Uni
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient

data class BookDto(
    val id: Long? = null,
    val title: String = "",
    val author: String = "",
    val isbn: String = "",
    val available: Boolean = true
)

@RegisterRestClient(configKey = "book-catalog-client")
@Path("/books")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
interface BookCatalogClient {

    @GET
    @Path("/{id}")
    fun getBook(@PathParam("id") id: Long): Uni<BookDto?>

    @PATCH
    @Path("/{id}/availability")
    fun updateAvailability(
        @PathParam("id") id: Long,
        @QueryParam("available") available: Boolean
    ): Uni<BookDto?>
}
