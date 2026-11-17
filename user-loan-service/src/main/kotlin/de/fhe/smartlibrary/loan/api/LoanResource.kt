package de.fhe.smartlibrary.loan.api

import de.fhe.smartlibrary.loan.application.LoanService
import de.fhe.smartlibrary.loan.domain.Loan
import io.smallrye.mutiny.Uni
import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response

@Path("/loans")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class LoanResource {

    @Inject
    lateinit var loanService: LoanService

    @GET
    fun getAllLoans(): Uni<List<Loan>> =
        loanService.getAllLoans()

    @GET
    @Path("/{id}")
    fun getLoan(@PathParam("id") id: Long): Uni<Response> =
        loanService.getLoanById(id).map { loan ->
            if (loan != null) Response.ok(loan).build()
            else Response.status(Response.Status.NOT_FOUND).build()
        }

    @POST
    fun createLoan(loan: Loan): Uni<Response> =
        loanService.createLoan(loan).map {
            Response.status(Response.Status.CREATED).entity(it).build()
        }

    @PATCH
    @Path("/{id}/return")
    fun returnLoan(@PathParam("id") id: Long): Uni<Response> =
        loanService.returnLoan(id).map { loan ->
            if (loan != null) Response.ok(loan).build()
            else Response.status(Response.Status.NOT_FOUND).build()
        }

    @GET
    @Path("/user/{userId}")
    fun getActiveLoansByUser(@PathParam("userId") userId: Long): Uni<List<Loan>> =
        loanService.getActiveLoansByUserId(userId)

    @GET
    @Path("/book/{bookId}")
    fun getActiveLoansByBook(@PathParam("bookId") bookId: Long): Uni<List<Loan>> =
        loanService.getActiveLoansByBookId(bookId)

    @DELETE
    @Path("/{id}")
    fun deleteLoan(@PathParam("id") id: Long): Uni<Response> =
        loanService.deleteLoan(id).map { deleted ->
            if (deleted) Response.noContent().build()
            else Response.status(Response.Status.NOT_FOUND).build()
        }
}
