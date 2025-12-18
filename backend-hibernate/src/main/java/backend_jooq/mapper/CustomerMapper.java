package backend_jooq.mapper;

import backend_jooq.api.dtos.ApiCustomer;
import backend_jooq.db.dtos.DbCustomer;

public final class CustomerMapper {
    private CustomerMapper() {}

    public static ApiCustomer dbToApi(DbCustomer db) {
        return new ApiCustomer(db.customerId(), db.firstName(), db.lastName(), db.email());
    }
}
