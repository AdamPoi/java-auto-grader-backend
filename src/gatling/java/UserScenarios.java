import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.UUID;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class UserScenarios extends Simulation {

    public static final HttpProtocolBuilder httpProtocol = http
            .baseUrl("http://localhost:8080")
            .acceptHeader("application/json")
            .contentTypeHeader("application/json");
    private static String adminAccessToken;
    private static String adminRefreshToken;
    static ScenarioBuilder scenario = scenario("User CRUD Management")
            .exec(session -> session
                    .set("authToken", adminAccessToken)
                    .set("refreshToken", adminRefreshToken)
            )

            .exec(session -> {
                String uuid = UUID.randomUUID().toString().substring(0, 8);
                return session
                        .set("email", "student_" + uuid + "@example.com")
                        .set("firstName", "Student")
                        .set("lastName", "Test" + uuid);
            })
            .exec(http("Create Student User")
                    .post("/api/users")
                    .header("Authorization", "Bearer #{authToken}")
                    .body(StringBody(session -> String.format("""
                                    {
                                      "email": "%s",
                                      "password": "student123",
                                      "firstName": "%s",
                                      "lastName": "%s",
                                      "roles": ["student"]
                                    }
                                    """,
                            session.getString("email"),
                            session.getString("firstName"),
                            session.getString("lastName")
                    )))
                    .check(status().is(201))
                    .check(jsonPath("$.data.id").exists().saveAs("userId"))
            ).exitHereIfFailed()
            .pause(1)

            .exec(http("Get Created User")
                    .get("/api/users/#{userId}")
                    .header("Authorization", "Bearer #{authToken}")
                    .check(status().is(200))
            ).exitHereIfFailed()
            .pause(1)

            .exec(http("Update User")
                    .patch("/api/users/#{userId}")
                    .header("Authorization", "Bearer #{authToken}")
                    .body(StringBody("""
                            {
                              "firstName": "UpdatedName",
                              "lastName": "UpdatedLast"
                            }
                            """))
                    .check(status().is(200))
            ).exitHereIfFailed()
            .pause(1)

            .exec(http("Delete User")
                    .delete("/api/users/#{userId}")
                    .header("Authorization", "Bearer #{authToken}")
                    .check(status().is(204))
            ).exitHereIfFailed();

    static {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/auth/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString("""
                            {
                              "email": "admin@example.com",
                              "password": "admin123"
                            }
                            """))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode json = mapper.readTree(response.body());
                adminAccessToken = json.at("/data/accessToken").asText();
                adminRefreshToken = json.at("/data/refreshToken").asText();
                System.out.println("✅ Admin logged in successfully");
            } else {
                throw new RuntimeException("Admin login failed: " + response.body());
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to login as admin before test");
        }
    }

    {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/api/auth/logout"))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + adminAccessToken)
                        .POST(HttpRequest.BodyPublishers.ofString(String.format("""
                                {
                                  "refreshToken": "%s"
                                }
                                """, adminRefreshToken)))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200 || response.statusCode() == 204) {
                    System.out.println("✅ Admin logout successful (after test)");
                } else {
                    System.err.println("❌ Logout failed: " + response.body());
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }));

        setUp(
                scenario.injectOpen(
                        atOnceUsers(10),
                        rampUsers(50).during(Duration.ofSeconds(30))
                )
        ).protocols(httpProtocol);
    }
}
