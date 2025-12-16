package dev.rabauer.hibernate_vs_jooq.backend_jooq.db.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DbInvoice(Integer invoiceId, LocalDate invoiceDate, BigDecimal totalAmount) {
}
