package dev.rabauer.benchmark;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;
import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ApiSimulation extends Simulation {

    HttpProtocolBuilder httpProtocol = http
            .baseUrl(System.getProperty("baseUrl", "http://localhost:8080"))
            .acceptHeader("application/json")
            .contentTypeHeader("application/json");

    AtomicInteger i = new AtomicInteger();

    ScenarioBuilder scn = scenario("Basic API scenario")
            .exec(http("List customers")
                    .get("/customer")
                    .check(status().is(200)))
            .pause(Duration.ofMillis(200))
            .exec(http("Create customer")
                    .post("/customer")
                    .body(StringBody(session -> {
                        long now = System.currentTimeMillis();
                        return String.format(
                                "{\"firstName\":\"%s\",\"lastName\":\"%s\",\"email\":\"%s\"}",
                                "PerfFirstName",
                                "User" + (now % 10_000),
                                "perf" + now + "_" + i.incrementAndGet() + "@example.com"
                        );
                    }))
                    .asJson()
                    .check(status().in(200, 201), header("Location").saveAs("location")));

    {
        // default: quick smoke test with 10 users arriving at once
        setUp(scn.injectOpen(atOnceUsers(10_000))).protocols(httpProtocol);
    }
}
