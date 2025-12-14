package dev.rabauer.hibernate_vs_jooq.backend_jooq.api;

import dev.rabauer.hibernate_vs_jooq.backend_jooq.db.CustomerRepository;
import dev.rabauer.hibernate_vs_jooq.backend_jooq.mapper.CustomerMapper;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MainResource {

    private final CustomerRepository customerRepository;

    public MainResource(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @GET
    @Path("/customers")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ApiCustomer> getAllCustomers() {
        return customerRepository.findAll().stream().map(CustomerMapper::dbToApi).toList();
    }
}
