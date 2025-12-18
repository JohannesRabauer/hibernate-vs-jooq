package backend_jooq.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "invoice")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invoice_id")
    private Integer invoiceId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "invoice_date", nullable = false)
    private LocalDate invoiceDate;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InvoiceItem> items = new ArrayList<>();

    public Invoice() {}

    public Invoice(Customer customer, LocalDate invoiceDate, BigDecimal totalAmount) {
        this.customer = customer;
        this.invoiceDate = invoiceDate;
        this.totalAmount = totalAmount;
    }

    public Integer getInvoiceId() { return invoiceId; }
    public LocalDate getInvoiceDate() { return invoiceDate; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public List<InvoiceItem> getItems() { return items; }
}
