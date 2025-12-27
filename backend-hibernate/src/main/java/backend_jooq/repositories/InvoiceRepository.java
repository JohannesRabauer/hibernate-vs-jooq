package backend_jooq.repositories;

import backend_jooq.api.dtos.ApiNewInvoice;
import backend_jooq.db.dtos.DbInvoice;
import backend_jooq.db.dtos.DbInvoiceItem;
import backend_jooq.model.Invoice;
import backend_jooq.model.InvoiceItem;
import backend_jooq.model.Product;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@ApplicationScoped
public class InvoiceRepository {

    private final EntityManager em;

    public InvoiceRepository(EntityManager em) {
        this.em = em;
    }

    public List<DbInvoice> findAllByCustomerId(int customerId) {
        var cb = em.getCriteriaBuilder();
        var cq = cb.createQuery(Invoice.class);
        var root = cq.from(Invoice.class);
        cq.select(root).where(cb.equal(root.get("customer").get("customerId"), customerId));
        List<Invoice> results = em.createQuery(cq).getResultList();
        return results.stream()
                .map(i -> new DbInvoice(i.getInvoiceId(), i.getInvoiceDate(), i.getTotalAmount()))
                .toList();
    }

    public List<DbInvoiceItem> findItemsByInvoiceId(int invoiceId) {
        var cb = em.getCriteriaBuilder();
        var cq = cb.createQuery(InvoiceItem.class);
        var root = cq.from(InvoiceItem.class);
        cq.select(root).where(cb.equal(root.get("invoice").get("invoiceId"), invoiceId));
        List<InvoiceItem> results = em.createQuery(cq).getResultList();
        return results.stream()
                .map(it -> new DbInvoiceItem(it.getQuantity(), it.getUnitPrice(), it.getProduct().getProductName(), it.getProduct().getPrice()))
                .toList();
    }

    @Transactional
    public Integer createInvoice(ApiNewInvoice newInvoice) {
        var customer = em.find(backend_jooq.model.Customer.class, newInvoice.customerId());
        var date = LocalDate.ofInstant(newInvoice.timestamp(), ZoneOffset.UTC);

        BigDecimal total = newInvoice.invoiceItemList().stream()
                .map(i -> BigDecimal.valueOf(i.price()).multiply(BigDecimal.valueOf(i.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        var invoice = new Invoice(customer, date, total);
        em.persist(invoice);
        em.flush();

        for (var item : newInvoice.invoiceItemList()) {
            var product = new Product(item.productName(), BigDecimal.valueOf(item.price()));
            em.persist(product);
            var invItem = new InvoiceItem(invoice, product, item.quantity(), BigDecimal.valueOf(item.price()));
            em.persist(invItem);
        }

        return invoice.getInvoiceId();
    }
}
