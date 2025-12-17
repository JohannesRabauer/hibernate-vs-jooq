package dev.rabauer.hibernate_vs_jooq.frontend;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import dev.rabauer.hibernate_vs_jooq.frontend.client.BackendClient;
import dev.rabauer.hibernate_vs_jooq.frontend.dto.*;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import java.time.Instant;

@Route("")
public class MainView extends VerticalLayout {

    @Inject
    @RestClient
    BackendClient client;

    private Grid<ApiCustomer> customerGrid = new Grid<>(ApiCustomer.class, false);

    public MainView() {
        setSizeFull();
        add(new H3("Customers"));
        customerGrid.addColumn(c -> c.firstName() + " " + c.lastName()).setHeader("Name");
        customerGrid.addColumn(c -> c.email()).setHeader("Email");
        customerGrid.setSelectionMode(Grid.SelectionMode.SINGLE);

        Button addCustomer = new Button("Add Customer", e -> openAddCustomer());
        Button view = new Button("View Details", e -> {
            var sel = customerGrid.asSingleSelect().getValue();
            if (sel != null) openCustomerDetails(sel);
        });

        HorizontalLayout actions = new HorizontalLayout(addCustomer, view);
        add(actions, customerGrid);
        setFlexGrow(1, customerGrid);
    }

    @PostConstruct
    void init() {
        refreshCustomers();
    }

    private void refreshCustomers() {
        var list = client.getCustomers();
        customerGrid.setItems(list);
    }

    private void openAddCustomer() {
        Dialog d = new Dialog();
        d.add(new H3("Add Customer"));
        FormLayout form = new FormLayout();
        TextField first = new TextField("First name");
        TextField last = new TextField("Last name");
        TextField email = new TextField("Email");
        form.add(first, last, email);

        Button submit = new Button("Create", e -> {
            ApiNewCustomer payload = new ApiNewCustomer(first.getValue(), last.getValue(), email.getValue());
            var created = client.createCustomer(payload);
            d.close();
            refreshCustomers();
        });
        Button cancel = new Button("Cancel", e -> d.close());
        d.add(form, new HorizontalLayout(submit, cancel));
        d.open();
    }

    private void openCustomerDetails(ApiCustomer customer) {
        Dialog d = new Dialog();
        d.setWidth("800px");
        d.add(new H3("Customer: " + customer.firstName() + " " + customer.lastName()));

        // Addresses
        Grid<ApiAddress> addrGrid = new Grid<>(ApiAddress.class, false);
        addrGrid.addColumn(a -> a.street()).setHeader("Street");
        addrGrid.addColumn(a -> a.city()).setHeader("City");
        addrGrid.addColumn(a -> a.country()).setHeader("Country");
        addrGrid.setItems(client.getAddresses(customer.id()));

        // Invoices
        Grid<ApiInvoice> invoiceGrid = new Grid<>(ApiInvoice.class, false);
        invoiceGrid.addColumn(i -> i.id()).setHeader("ID");
        invoiceGrid.addColumn(i -> i.timestamp()).setHeader("Date");
        invoiceGrid.addColumn(i -> i.totalAmount()).setHeader("Total");
        invoiceGrid.setItems(client.getInvoices(customer.id()));

        Button createInvoice = new Button("Create Invoice", e -> openCreateInvoice(customer));
        Button viewInvoice = new Button("View Invoice", e -> {
            var sel = invoiceGrid.asSingleSelect().getValue();
            if (sel != null) openInvoiceDetail(sel.id());
        });

        d.add(new H3("Addresses"), addrGrid, new H3("Invoices"), invoiceGrid, new HorizontalLayout(createInvoice, viewInvoice));
        d.open();
    }

    private void openInvoiceDetail(int invoiceId) {
        ApiInvoiceDetail detail = client.getInvoiceDetail(invoiceId);
        Dialog d = new Dialog();
        d.add(new H3("Invoice " + invoiceId));
        Grid<ApiInvoiceDetail.ApiInvoiceItem> items = new Grid<>(ApiInvoiceDetail.ApiInvoiceItem.class, false);
        items.addColumn(it -> it.product().name()).setHeader("Product");
        items.addColumn(ApiInvoiceDetail.ApiInvoiceItem::quantity).setHeader("Qty");
        items.addColumn(it -> it.product().price()).setHeader("Unit Price");
        items.setItems(detail.items());
        d.add(items, new Button("Close", e -> d.close()));
        d.open();
    }

    private void openCreateInvoice(ApiCustomer customer) {
        Dialog d = new Dialog();
        d.setWidth("600px");
        d.add(new H3("Create Invoice for " + customer.firstName() + " " + customer.lastName()));

        FormLayout form = new FormLayout();
        TextField product = new TextField("Product name");
        NumberField price = new NumberField("Price");
        price.setValue(0.0);
        NumberField quantity = new NumberField("Quantity");
        quantity.setValue(1.0);
        form.add(product, price, quantity);

        Button submit = new Button("Create", e -> {
            ApiNewInvoice.ApiNewInvoiceItem item = new ApiNewInvoice.ApiNewInvoiceItem(product.getValue(), price.getValue(), quantity.getValue().intValue());
            ApiNewInvoice inv = new ApiNewInvoice(customer.id(), Instant.now(), java.util.List.of(item));
            client.createInvoice(inv);
            d.close();
        });
        Button cancel = new Button("Cancel", e -> d.close());
        d.add(form, new HorizontalLayout(submit, cancel));
        d.open();
    }
}
