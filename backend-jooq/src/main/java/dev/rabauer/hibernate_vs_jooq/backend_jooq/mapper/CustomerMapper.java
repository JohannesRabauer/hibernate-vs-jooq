package dev.rabauer.hibernate_vs_jooq.backend_jooq.mapper;

import dev.rabauer.hibernate_vs_jooq.backend_jooq.api.ApiCustomer;
import dev.rabauer.hibernate_vs_jooq.backend_jooq.db.DbCustomer;

public final class CustomerMapper {

    private CustomerMapper() {
    }

    public static ApiCustomer dbToApi(DbCustomer dbCustomer)
    {
        return new ApiCustomer(
                dbCustomer.customerId(),
                dbCustomer.firstName(),
                dbCustomer.lastName(),
                dbCustomer.email()
        );
    }
}
