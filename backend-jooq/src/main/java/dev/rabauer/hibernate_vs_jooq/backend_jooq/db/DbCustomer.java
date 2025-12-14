package dev.rabauer.hibernate_vs_jooq.backend_jooq.db;

public record DbCustomer(
        Integer customerId, String firstName, String lastName, String email
) {
}
