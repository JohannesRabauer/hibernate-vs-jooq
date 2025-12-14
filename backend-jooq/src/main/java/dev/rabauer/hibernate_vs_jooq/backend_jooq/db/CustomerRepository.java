package dev.rabauer.hibernate_vs_jooq.backend_jooq.db;

import jakarta.enterprise.context.ApplicationScoped;
import org.jooq.DSLContext;

import java.util.List;

import static dev.rabauer.hibernate_vs_jooq.backend_jooq.generated.tables.Customer.CUSTOMER;

@ApplicationScoped
public class CustomerRepository {

    private final DSLContext dsl;

    public CustomerRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public List<DbCustomer> findAll()
    {
        return dsl.selectFrom(CUSTOMER)
                .fetchInto(DbCustomer.class);
    }
}
