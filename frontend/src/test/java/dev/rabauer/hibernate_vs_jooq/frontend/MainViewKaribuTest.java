package dev.rabauer.hibernate_vs_jooq.frontend;

import com.github.mvysny.kaributesting.v10.MockVaadin;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.provider.Query;
import dev.rabauer.hibernate_vs_jooq.frontend.dto.ApiCustomer;
import dev.rabauer.hibernate_vs_jooq.frontend.dto.ApiInvoice;
import dev.rabauer.hibernate_vs_jooq.frontend.dto.ApiInvoiceDetail;
import io.quarkus.test.InjectMock;
import io.quarkus.test.Mock;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import dev.rabauer.hibernate_vs_jooq.frontend.client.BackendClient;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@org.junit.jupiter.api.Tag("ui")
@io.quarkus.test.junit.QuarkusTest
public class MainViewKaribuTest {

    @Inject
    Instance<MainView> mainViewInstance;

    @InjectMock
    @RestClient
    BackendClient backendClient;

    @BeforeEach
    void setup() {
        // mock backend client responses before creating view
        org.mockito.Mockito.when(backendClient.getCustomers()).thenReturn(java.util.List.of(new ApiCustomer(1, "Alice", "Smith", "alice@example.com")));
        org.mockito.Mockito.when(backendClient.getAddresses(org.mockito.Mockito.anyInt())).thenReturn(java.util.List.of(new dev.rabauer.hibernate_vs_jooq.frontend.dto.ApiAddress("Main St 1", "Town", "Country")));
        ApiInvoice inv = new ApiInvoice(1, java.time.Instant.parse("2025-01-01T00:00:00Z"), 2500);
        org.mockito.Mockito.when(backendClient.getInvoices(org.mockito.Mockito.anyInt())).thenReturn(java.util.List.of(inv));
        ApiInvoiceDetail.ApiProduct prod = new ApiInvoiceDetail.ApiProduct( "Gadget", 12.5);
        ApiInvoiceDetail.ApiInvoiceItem item = new ApiInvoiceDetail.ApiInvoiceItem(2, prod);
        ApiInvoiceDetail detail = new ApiInvoiceDetail(java.util.List.of(item));
        org.mockito.Mockito.when(backendClient.getInvoiceDetail(org.mockito.Mockito.anyInt())).thenReturn(detail);

        MockVaadin.setup();
    }

    @AfterEach
    void tearDown() {
        MockVaadin.tearDown();
    }

    @Test
    public void showsCustomersInGrid() {
        MainView view = mainViewInstance.get();
        // customer grid is the first grid added to the view
        Grid<?> grid = view.getChildren().filter(c -> c instanceof Grid).map(c -> (Grid<?>) c).findFirst().orElse(null);
        assertNotNull(grid, "Customer grid should be present");

        List<?> items = grid.getDataProvider().fetch(new com.vaadin.flow.data.provider.Query<>()).collect(Collectors.toList());
        assertEquals(1, items.size());
        Object first = items.get(0);
        assertTrue(first instanceof ApiCustomer);
        ApiCustomer customer = (ApiCustomer) first;
        assertEquals("Alice", customer.firstName());
    }
}
