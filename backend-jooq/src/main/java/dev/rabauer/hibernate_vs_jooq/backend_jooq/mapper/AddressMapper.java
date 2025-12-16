package dev.rabauer.hibernate_vs_jooq.backend_jooq.mapper;

import dev.rabauer.hibernate_vs_jooq.backend_jooq.api.dtos.ApiAddress;
import dev.rabauer.hibernate_vs_jooq.backend_jooq.db.dtos.DbAddress;

public final class AddressMapper {

    private AddressMapper() {
    }

    public static ApiAddress dbToApi(DbAddress db) {
        return new ApiAddress(db.street(), db.city(), db.country());
    }
}
