package dev.rabauer.hibernate_vs_jooq.backend_jooq.api;

import dev.rabauer.hibernate_vs_jooq.backend_jooq.api.dtos.*;
import dev.rabauer.hibernate_vs_jooq.backend_jooq.db.dtos.DbCustomer;
import dev.rabauer.hibernate_vs_jooq.backend_jooq.db.repositories.AddressRepository;
import dev.rabauer.hibernate_vs_jooq.backend_jooq.db.repositories.CustomerRepository;
import dev.rabauer.hibernate_vs_jooq.backend_jooq.db.repositories.InvoiceRepository;
import dev.rabauer.hibernate_vs_jooq.backend_jooq.mapper.AddressMapper;
import dev.rabauer.hibernate_vs_jooq.backend_jooq.mapper.CustomerMapper;
import dev.rabauer.hibernate_vs_jooq.backend_jooq.mapper.InvoiceMapper;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MainResource {

    private final CustomerRepository customerRepository;
    private final AddressRepository addressRepository;
    private final InvoiceRepository invoiceRepository;

    public MainResource(CustomerRepository customerRepository, AddressRepository addressRepository, InvoiceRepository invoiceRepository) {
        this.customerRepository = customerRepository;
        this.addressRepository = addressRepository;
        this.invoiceRepository = invoiceRepository;
    }

    @GET
    @Path("/customer")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ApiCustomer> getAllCustomers() {
        return customerRepository.findAll().stream().map(CustomerMapper::dbToApi).toList();
    }

    @GET
    @Path("/customer/{customerId}/addresses")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ApiAddress> getAllAddressesOfCustomer(@PathParam("customerId") int customerId) {
        return addressRepository.findAllByCustomerId(customerId).stream().map(AddressMapper::dbToApi).toList();
    }

    @GET
    @Path("/customer/{customerId}/invoices")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ApiInvoice> getAllInvoicesOfCustomer(@PathParam("customerId") int customerId) {
        return invoiceRepository.findAllByCustomerId(customerId).stream().map(InvoiceMapper::dbToApi).toList();
    }

    @GET
    @Path("/invoice/{invoiceId}")
    @Produces(MediaType.APPLICATION_JSON)
    public ApiInvoiceDetail getInvoiceDetail(@PathParam("invoiceId") int invoiceId) {
        return InvoiceMapper.itemsToApiDetail(invoiceRepository.findItemsByInvoiceId(invoiceId));
    }

    @POST
    @Path("/customer")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createCustomer(@Valid ApiNewCustomer newCustomer) {
        DbCustomer db = customerRepository.create(newCustomer.firstName(), newCustomer.lastName(), newCustomer.email());
        ApiCustomer api = CustomerMapper.dbToApi(db);
        return Response.created(java.net.URI.create("/customer/" + api.id())).entity(api).build();
    }

    @POST
    @Path("/invoice")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createInvoice(@Valid ApiNewInvoice newInvoice) {
        int id = invoiceRepository.createInvoice(newInvoice);
        return Response.created(java.net.URI.create("/invoice/" + id)).build();
    }
}
