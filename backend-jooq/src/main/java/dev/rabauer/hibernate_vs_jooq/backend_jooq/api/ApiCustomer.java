package dev.rabauer.hibernate_vs_jooq.backend_jooq.api;

public record ApiCustomer(
        Integer customerId, String firstName, String lastName, String email
) {
}

