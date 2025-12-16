package dev.rabauer.hibernate_vs_jooq.backend_jooq.db.dtos;

import java.math.BigDecimal;

public record DbInvoiceItem(int quantity, BigDecimal unitPrice, String productName, BigDecimal productPrice) {
}
