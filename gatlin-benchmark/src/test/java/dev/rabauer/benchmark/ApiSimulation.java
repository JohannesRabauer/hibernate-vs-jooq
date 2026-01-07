package dev.rabauer.benchmark;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;
import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

public class ApiSimulation extends Simulation {

    HttpProtocolBuilder httpProtocol = http
            .baseUrl(System.getProperty("baseUrl", "http://localhost:8080"))
            .acceptHeader("application/json")
            .contentTypeHeader("application/json");

    AtomicInteger i = new AtomicInteger();
    // Parameters (override with -Dproperty=value)
    int totalRequests = Integer.getInteger("totalRequests", 1000);
    int concurrentUsers = Integer.getInteger("concurrentUsers", 10);
    int repeatsPerUser = Math.max(1, totalRequests / concurrentUsers);

    // Scenario: repeated GET /customer (useful to measure pure GET latency at load)
    ScenarioBuilder bulkGet = scenario("Bulk GET /customer")
            .repeat(repeatsPerUser).on(exec(http("List customers")
                    .get("/customer")
                    .check(status().is(200)))
                    .pause(Duration.ofMillis(50)));

    // Scenario: mixed workload (list -> create -> get created) for more realistic behaviour
    ScenarioBuilder mixed = scenario("Mixed create/get scenario")
            .exec(http("List customers")
                    .get("/customer")
                    .check(status().is(200)))
            .pause(Duration.ofMillis(100))
            .exec(http("Create customer")
                    .post("/customer")
                    .body(StringBody(session -> {
                        long now = System.currentTimeMillis();
                        return String.format(
                                "{\"firstName\":\"%s\",\"lastName\":\"%s\",\"email\":\"%s\"}",
                                "PerfFirstName",
                                "User" + (now % 1_000),
                                "perf" + now + "_" + i.incrementAndGet() + "@example.com"
                        );
                    }))
                    .asJson()
                    .check(status().in(200, 201), header("Location").saveAs("location")))
            .doIf(session -> session.contains("location")).then(exec(
                    http("Get created")
                            .get(session -> session.getString("location"))
                            .check(status().is(200))
            ));

    // Scenario: single request to measure a single response time (use -Dmode=single)
    ScenarioBuilder single = scenario("Single GET measure")
            .exec(http("List customers")
                    .get("/customer")
                    .check(status().is(200)));

    // Choose mode with -Dmode=single | mixed | bulk (default: bulk)
    String mode = System.getProperty("mode", "bulk");

    {
    if("single".equalsIgnoreCase(mode))
    {
        // one user, one request (good for measuring raw latency)
        setUp(single.injectOpen(atOnceUsers(1))).protocols(httpProtocol);
    } else if ("mixed".equalsIgnoreCase(mode)) {
        // concurrentUsers executing the mixed workload once each (you can tune injection)
        setUp(mixed.injectOpen(atOnceUsers(concurrentUsers))).protocols(httpProtocol);
    } else {
        // bulk mode: totalRequests spread across concurrentUsers (default)
        setUp(bulkGet.injectOpen(atOnceUsers(concurrentUsers))).protocols(httpProtocol);
    }
    }
}
