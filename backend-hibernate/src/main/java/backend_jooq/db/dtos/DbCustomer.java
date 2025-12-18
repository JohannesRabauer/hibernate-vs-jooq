package backend_jooq.db.dtos;

public record DbCustomer(Integer customerId, String firstName, String lastName, String email) {
}
