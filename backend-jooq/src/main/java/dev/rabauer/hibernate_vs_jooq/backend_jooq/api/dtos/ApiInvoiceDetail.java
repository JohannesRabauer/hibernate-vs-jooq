package dev.rabauer.hibernate_vs_jooq.backend_jooq.api.dtos;

import java.util.List;

public record ApiInvoiceDetail(List<ApiInvoiceItem> items) {

    public record ApiInvoiceItem(int quantity, ApiProduct product) {
    }

    public record ApiProduct(String name, double price) {
    }
}
