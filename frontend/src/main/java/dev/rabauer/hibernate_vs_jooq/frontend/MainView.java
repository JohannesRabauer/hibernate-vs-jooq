package dev.rabauer.hibernate_vs_jooq.frontend;

import com.github.javafaker.Faker;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import dev.rabauer.hibernate_vs_jooq.frontend.client.BackendClient;
import dev.rabauer.hibernate_vs_jooq.frontend.dto.*;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

import java.time.Instant;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.vaadin.flow.component.notification.Notification;

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
        customerGrid.addColumn(ApiCustomer::email).setHeader("Email");
        customerGrid.setSelectionMode(Grid.SelectionMode.SINGLE);

        Button addCustomer = new Button("Add Customer", e -> openAddCustomer());

        NumberField bulkCount = new NumberField("Create customers");
        bulkCount.setValue(10_000.0);
        bulkCount.setStep(1_000);
        bulkCount.setMin(1);

        Button generateCustomers = new Button("Generate");
        generateCustomers.addClickListener(e -> {
            int count = bulkCount.getValue() == null ? 0 : bulkCount.getValue().intValue();
            if (count <= 0) return;

            // disable controls while running
            generateCustomers.setEnabled(false);
            bulkCount.setEnabled(false);

            var start = Instant.now();
            System.out.println("Starting customer generation: count=" + count + " at " + start);

            java.util.concurrent.CompletableFuture.runAsync(() -> {
                // Ensure the thread has the correct context class loader so the REST client and JSON providers
                // can find the DTO classes when executing on the common ForkJoin pool.
                ClassLoader prevCl = Thread.currentThread().getContextClassLoader();
                Thread.currentThread().setContextClassLoader(MainView.class.getClassLoader());
                try {
                    Faker faker = new Faker();
                    AtomicInteger success = new java.util.concurrent.atomic.AtomicInteger(0);
                    for (int i = 1; i <= count; i++) {
                        ApiNewCustomer payload = new ApiNewCustomer(faker.name().firstName(), faker.name().lastName(), faker.internet().emailAddress(Instant.now().getEpochSecond() + "" + i));
                        try {
                            client.createCustomer(payload);
                            success.incrementAndGet();
                        } catch (Exception ex) {
                            System.err.println("Failed creating customer #" + i + ": " + ex.getMessage());
                        }
                        if (i % 1000 == 0) {
                            System.out.println("Created " + i + " customers so far at " + Instant.now());
                            // Show a short progress notification on the UI thread
                            final int createdAmount = i;
                            getUI().ifPresent(ui -> ui.access(() -> {
                                Notification.show("Created " + createdAmount + " customers so far", 1000, Notification.Position.TOP_CENTER);
                            }));
                        }
                    }

                    Instant end = Instant.now();
                    long duration = Duration.between(start, end).toMillis();
                    int succ = success.get();
                    System.out.println("Finished customer generation at " + end + " (duration: " + duration + " ms), created: " + succ + "/" + count);

                    getUI().ifPresent(ui -> ui.access(() -> {
                        Notification.show("Finished generation: " + succ + " / " + count + " customers in " + duration + " ms", 5000, Notification.Position.TOP_CENTER);
                        refreshCustomers();
                        generateCustomers.setEnabled(true);
                        bulkCount.setEnabled(true);
                    }));
                } finally {
                    Thread.currentThread().setContextClassLoader(prevCl);
                }
            }).exceptionally(ex -> {
                ex.printStackTrace();
                getUI().ifPresent(ui -> ui.access(() -> {
                    generateCustomers.setEnabled(true);
                    bulkCount.setEnabled(true);
                }));
                return null;
            });
        });

        // Open details when clicking a customer row
        customerGrid.addItemClickListener(event -> {
            openCustomerDetails(event.getItem());
        });

        HorizontalLayout actions = new HorizontalLayout(addCustomer, bulkCount, generateCustomers);
        add(actions, customerGrid);
        setFlexGrow(1, customerGrid);
    }

    @PostConstruct
    void init() {
        refreshCustomers();
    }

    private void refreshCustomers() {
        var start = Instant.now();
        System.out.println("Fetching customers: start=" + start);

        List<ApiCustomer> list;
        try {
            list = client.getCustomers();
        } catch (Exception ex) {
            System.err.println("Failed to fetch customers: " + ex.getMessage());
            Notification.show("Failed to load customers: " + ex.getMessage(), 3000, Notification.Position.TOP_CENTER);
            return;
        }

        var end = Instant.now();
        long duration = Duration.between(start, end).toMillis();
        System.out.println("Fetching customers: end=" + end + " duration=" + duration + " ms");
        Notification.show("Loaded " + list.size() + " customers in " + duration + " ms", 5000, Notification.Position.TOP_CENTER);

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
            ApiCustomer created = client.createCustomer(payload);
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
        addrGrid.addColumn(ApiAddress::street).setHeader("Street");
        addrGrid.addColumn(ApiAddress::city).setHeader("City");
        addrGrid.addColumn(ApiAddress::country).setHeader("Country");
        addrGrid.setItems(client.getAddresses(customer.id()));

        // Invoices
        Grid<ApiInvoice> invoiceGrid = new Grid<>(ApiInvoice.class, false);
        invoiceGrid.addColumn(ApiInvoice::id).setHeader("ID");
        invoiceGrid.addColumn(ApiInvoice::timestamp).setHeader("Date");
        invoiceGrid.addColumn(ApiInvoice::totalAmount).setHeader("Total");
        invoiceGrid.setItems(client.getInvoices(customer.id()));
        // open invoice detail when clicking a row
        invoiceGrid.addItemClickListener(event -> openInvoiceDetail(event.getItem().id()));
        Button createInvoice = new Button("Create Invoice", e -> openCreateInvoice(customer));

        d.add(new H3("Addresses"), addrGrid, new H3("Invoices"), invoiceGrid, new HorizontalLayout(createInvoice));
        d.open();
    }

    private void openInvoiceDetail(int invoiceId) {
        ApiInvoiceDetail detail = client.getInvoiceDetail(invoiceId);
        Dialog d = new Dialog();
        d.setWidth("500px");
        d.add(new H3("Invoice " + invoiceId));
        Grid<ApiInvoiceDetail.ApiInvoiceItem> items = new Grid<>(ApiInvoiceDetail.ApiInvoiceItem.class, false);
        items.addColumn(it -> it.product().name()).setHeader("Product");
        items.addColumn(ApiInvoiceDetail.ApiInvoiceItem::quantity).setHeader("Qty");
        items.addColumn(it -> it.product().price()).setHeader("Unit Price");
        items.setItems(detail.items());
        d.add(items);
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
