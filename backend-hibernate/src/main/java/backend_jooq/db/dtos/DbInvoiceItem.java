package backend_jooq.db.dtos;

import java.math.BigDecimal;

public record DbInvoiceItem(int quantity, BigDecimal unitPrice, String productName, BigDecimal productPrice) {
}
