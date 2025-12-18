package backend_jooq.api.dtos;

import java.time.Instant;

public record ApiInvoice(int id, Instant timestamp, int totalAmount) {
}

