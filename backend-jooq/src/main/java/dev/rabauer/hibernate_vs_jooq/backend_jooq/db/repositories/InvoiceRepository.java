package dev.rabauer.hibernate_vs_jooq.backend_jooq.db.repositories;

import dev.rabauer.hibernate_vs_jooq.backend_jooq.api.dtos.ApiNewInvoice;
import dev.rabauer.hibernate_vs_jooq.backend_jooq.db.dtos.DbInvoice;
import dev.rabauer.hibernate_vs_jooq.backend_jooq.db.dtos.DbInvoiceItem;
import jakarta.enterprise.context.ApplicationScoped;
import org.jooq.DSLContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static dev.rabauer.hibernate_vs_jooq.backend_jooq.generated.tables.Invoice.INVOICE;
import static dev.rabauer.hibernate_vs_jooq.backend_jooq.generated.tables.InvoiceItem.INVOICE_ITEM;
import static dev.rabauer.hibernate_vs_jooq.backend_jooq.generated.tables.Product.PRODUCT;

@ApplicationScoped
public class InvoiceRepository {

    private final DSLContext dsl;

    public InvoiceRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public List<DbInvoice> findAllByCustomerId(int customerId) {
        return dsl.select(INVOICE.INVOICE_ID, INVOICE.INVOICE_DATE, INVOICE.TOTAL_AMOUNT)
                .from(INVOICE)
                .where(INVOICE.CUSTOMER_ID.eq(customerId))
                .fetch(r -> new DbInvoice(
                        r.get(INVOICE.INVOICE_ID),
                        r.get(INVOICE.INVOICE_DATE),
                        r.get(INVOICE.TOTAL_AMOUNT)
                ));
    }

    public List<DbInvoiceItem> findItemsByInvoiceId(int invoiceId) {
        return dsl.select(INVOICE_ITEM.QUANTITY, INVOICE_ITEM.UNIT_PRICE, PRODUCT.PRODUCT_NAME, PRODUCT.PRICE)
                .from(INVOICE_ITEM)
                .join(PRODUCT).on(INVOICE_ITEM.PRODUCT_ID.eq(PRODUCT.PRODUCT_ID))
                .where(INVOICE_ITEM.INVOICE_ID.eq(invoiceId))
                .fetch(r -> new DbInvoiceItem(
                        r.get(INVOICE_ITEM.QUANTITY),
                        r.get(INVOICE_ITEM.UNIT_PRICE),
                        r.get(PRODUCT.PRODUCT_NAME),
                        r.get(PRODUCT.PRICE)
                ));
    }

    public Integer createInvoice(ApiNewInvoice newInvoice) {
        return dsl.transactionResult(configuration -> {
            var ctx = org.jooq.impl.DSL.using(configuration);

            // insert invoice
            var inserted = ctx.insertInto(INVOICE)
                    .set(INVOICE.CUSTOMER_ID, newInvoice.customerId())
                    .set(INVOICE.INVOICE_DATE, LocalDate.ofInstant(newInvoice.timestamp(), java.time.ZoneOffset.UTC))
                    .set(INVOICE.TOTAL_AMOUNT, newInvoice.invoiceItemList().stream()
                            .map(i -> BigDecimal.valueOf(i.price()).multiply(BigDecimal.valueOf(i.quantity())))
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                    )
                    .returning(INVOICE.INVOICE_ID)
                    .fetchOne();

            int invoiceId = inserted.getValue(INVOICE.INVOICE_ID);

            // insert products and items
            for (var item : newInvoice.invoiceItemList()) {
                // create product
                var prod = ctx.insertInto(PRODUCT)
                        .set(PRODUCT.PRODUCT_NAME, item.productName())
                        .set(PRODUCT.PRICE, BigDecimal.valueOf(item.price()))
                        .returning(PRODUCT.PRODUCT_ID)
                        .fetchOne();

                int productId = prod.getValue(PRODUCT.PRODUCT_ID);

                ctx.insertInto(INVOICE_ITEM)
                        .set(INVOICE_ITEM.INVOICE_ID, invoiceId)
                        .set(INVOICE_ITEM.PRODUCT_ID, productId)
                        .set(INVOICE_ITEM.QUANTITY, item.quantity())
                        .set(INVOICE_ITEM.UNIT_PRICE, BigDecimal.valueOf(item.price()))
                        .execute();
            }

            return invoiceId;
        });
    }
}
