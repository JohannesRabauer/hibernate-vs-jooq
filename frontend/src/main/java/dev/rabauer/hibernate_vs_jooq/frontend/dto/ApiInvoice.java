package dev.rabauer.hibernate_vs_jooq.frontend.dto;

import java.time.Instant;

public record ApiInvoice(int id, Instant timestamp, int totalAmount) {
}

