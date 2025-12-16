package dev.rabauer.hibernate_vs_jooq.backend_jooq.api.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;

public record ApiNewInvoice(
        @NotNull
        int customerId,
        @NotNull
        Instant timestamp,
        @NotEmpty
        List<ApiNewInvoiceItem> invoiceItemList
) {
    public record ApiNewInvoiceItem(
            @NotBlank
            String productName,
            @NotNull
            double price,
            @NotNull
            int quantity
    ) {
    }
}
