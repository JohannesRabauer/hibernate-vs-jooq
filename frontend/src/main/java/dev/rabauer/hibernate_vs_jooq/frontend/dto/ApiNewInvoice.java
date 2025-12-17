package dev.rabauer.hibernate_vs_jooq.frontend.dto;

import java.time.Instant;
import java.util.List;

public record ApiNewInvoice(
        int customerId,
        Instant timestamp,
        List<ApiNewInvoiceItem> invoiceItemList
) {
    public record ApiNewInvoiceItem(
            String productName,
            double price,
            int quantity
    ) {
    }
}
