package dev.rabauer.hibernate_vs_jooq.backend_jooq.mapper;

import dev.rabauer.hibernate_vs_jooq.backend_jooq.api.dtos.ApiInvoice;
import dev.rabauer.hibernate_vs_jooq.backend_jooq.api.dtos.ApiInvoiceDetail;
import dev.rabauer.hibernate_vs_jooq.backend_jooq.db.dtos.DbInvoice;
import dev.rabauer.hibernate_vs_jooq.backend_jooq.db.dtos.DbInvoiceItem;

import java.time.ZoneOffset;
import java.util.List;

public final class InvoiceMapper {

    private InvoiceMapper() {
    }

    public static ApiInvoice dbToApi(DbInvoice db) {
        var timestamp = db.invoiceDate().atStartOfDay().toInstant(ZoneOffset.UTC);
        int total = db.totalAmount() == null ? 0 : db.totalAmount().intValue();
        return new ApiInvoice(db.invoiceId(), timestamp, total);
    }

    public static ApiInvoiceDetail itemsToApiDetail(List<DbInvoiceItem> items) {
        var apiItems = items.stream().map(i -> new ApiInvoiceDetail.ApiInvoiceItem(
                i.quantity(),
                new ApiInvoiceDetail.ApiProduct(i.productName(), i.productPrice().doubleValue())
        )).toList();
        return new ApiInvoiceDetail(apiItems);
    }
}
