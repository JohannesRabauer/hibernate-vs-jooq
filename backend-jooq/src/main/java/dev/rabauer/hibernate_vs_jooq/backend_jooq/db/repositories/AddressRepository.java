package dev.rabauer.hibernate_vs_jooq.backend_jooq.db.repositories;

import dev.rabauer.hibernate_vs_jooq.backend_jooq.db.dtos.DbAddress;
import jakarta.enterprise.context.ApplicationScoped;
import org.jooq.DSLContext;

import java.util.List;

import static dev.rabauer.hibernate_vs_jooq.backend_jooq.generated.tables.Address.ADDRESS;

@ApplicationScoped
public class AddressRepository {

    private final DSLContext dsl;

    public AddressRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public List<DbAddress> findAllByCustomerId(int customerId) {
        return dsl.select(ADDRESS.STREET, ADDRESS.CITY, ADDRESS.COUNTRY)
                .from(ADDRESS)
                .where(ADDRESS.CUSTOMER_ID.eq(customerId))
                .fetchInto(DbAddress.class);
    }
}
