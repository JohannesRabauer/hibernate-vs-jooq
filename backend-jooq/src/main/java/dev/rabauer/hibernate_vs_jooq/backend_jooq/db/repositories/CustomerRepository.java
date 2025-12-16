package dev.rabauer.hibernate_vs_jooq.backend_jooq.db.repositories;

import dev.rabauer.hibernate_vs_jooq.backend_jooq.db.dtos.DbCustomer;
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

    public List<DbCustomer> findAll() {
        return dsl.selectFrom(CUSTOMER)
                .fetchInto(DbCustomer.class);
    }

    public DbCustomer create(String firstName, String lastName, String email) {
        var inserted = dsl.insertInto(CUSTOMER)
                .set(CUSTOMER.FIRST_NAME, firstName)
                .set(CUSTOMER.LAST_NAME, lastName)
                .set(CUSTOMER.EMAIL, email)
                .returning(CUSTOMER.CUSTOMER_ID)
                .fetchOne();

        return new DbCustomer(inserted.getValue(CUSTOMER.CUSTOMER_ID), firstName, lastName, email);
    }
}
