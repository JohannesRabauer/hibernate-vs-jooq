package dev.rabauer.hibernate_vs_jooq.frontend.client;

import dev.rabauer.hibernate_vs_jooq.frontend.dto.*;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/")
@RegisterRestClient(configKey = "backend-api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface BackendClient {

    @GET
    @Path("customer")
    List<ApiCustomer> getCustomers();

    @POST
    @Path("customer")
    ApiCustomer createCustomer(ApiNewCustomer newCustomer);

    @GET
    @Path("customer/{id}/addresses")
    List<ApiAddress> getAddresses(@PathParam("id") int customerId);

    @GET
    @Path("customer/{id}/invoices")
    List<ApiInvoice> getInvoices(@PathParam("id") int customerId);

    @POST
    @Path("invoice")
    Response createInvoice(ApiNewInvoice newInvoice);

    @GET
    @Path("invoice/{id}")
    ApiInvoiceDetail getInvoiceDetail(@PathParam("id") int invoiceId);
}
