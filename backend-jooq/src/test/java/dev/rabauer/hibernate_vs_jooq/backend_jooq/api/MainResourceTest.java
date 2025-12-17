package dev.rabauer.hibernate_vs_jooq.backend_jooq.api;

import dev.rabauer.hibernate_vs_jooq.backend_jooq.api.dtos.ApiNewCustomer;
import dev.rabauer.hibernate_vs_jooq.backend_jooq.api.dtos.ApiNewInvoice;
import dev.rabauer.hibernate_vs_jooq.backend_jooq.test.PostgresResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@QuarkusTestResource(PostgresResource.class)
public class MainResourceTest {

    @Test
    public void testCreateAndListCustomer() {
        // create
        int id = given()
                .contentType(JSON)
                .body(new ApiNewCustomer("Alice", "Smith", "alice@example.com"))
                .when().post("/customer")
                .then().statusCode(HttpStatus.SC_CREATED)
                .body("email", equalTo("alice@example.com"))
                .extract().path("id");

        // list
        given().when().get("/customer")
                .then().statusCode(200)
                .body("email", hasItem("alice@example.com"));
    }

    @Test
    public void testCreateInvoiceAndGetDetail() {
        // create customer
        int customerId = given()
                .contentType(JSON)
                .body(new ApiNewCustomer("Bob", "Jones", "bob@example.com"))
                .when().post("/customer")
                .then().statusCode(HttpStatus.SC_CREATED)
                .extract().path("id");

        String location = given()
                .contentType(JSON)
                .body(new ApiNewInvoice(
                        customerId,
                        Instant.now(),
                        List.of(
                                new ApiNewInvoice.ApiNewInvoiceItem("Gadget", 12.5, 2)
                        )
                ))
                .when().post("/invoice")
                .then().statusCode(201)
                .extract().header("Location");

        // invoice id from location
        String[] parts = location.split("/");
        int invoiceId = Integer.parseInt(parts[parts.length - 1]);

        // get customer invoices
        given().when().get("/customer/" + customerId + "/invoices")
                .then().statusCode(200)
                .body("id", hasItem(invoiceId));

        // get invoice detail
        given().when().get("/invoice/" + invoiceId)
                .then().statusCode(200)
                .body("items.size()", is(1))
                .body("items[0].product.name", equalTo("Gadget"));
    }
}
