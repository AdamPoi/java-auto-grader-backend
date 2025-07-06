import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class CompilerScenarios {


    public static final HttpProtocolBuilder httpProtocol = http
            .baseUrl("http://localhost:8080")
            .acceptHeader("application/json")
            .contentTypeHeader("application/json");
    private static final String adminRefreshToken;
    private static String adminAccessToken;
    static ScenarioBuilder scenario = scenario("User CRUD Management")
            .exec(session -> session
                    .set("authToken", adminAccessToken)
            )
            .exec(http("Run Code")
                    .post("/api/submission-codes/run")
                    .header("Authorization", session -> "Bearer " + session.getString("authToken"))
                    .body(StringBody("""
                            {
                              "files": [
                                {
                                  "fileName": "Main.java",
                                  "content": "public class Main {\\n  public static void main(String[] args) {\\n    System.out.println(\\"hello world\\");\\n  }\\n}"
                                }
                              ]
                            }
                            """))
                    .check(status().is(200))
            ).pause(1)

            .exec(http("Test Submission")
                    .post("/api/submissions/tryout")
                    .header("Authorization", session -> "Bearer " + session.getString("authToken"))
                    .body(StringBody("""
                            {
                              "assignmentId": "1f9a6f03-203f-45c7-9774-69cd7e82a1ad",
                              "sourceFiles": [
                                {
                                  "fileName": "Main.java",
                                  "content": "public class Main {\\n  public static void main(String[] args) {\\n    System.out.println(\\"hello world\\");\\n  }\\n}"
                                }
                              ],
                              "testFiles": [
                                {
                                  "fileName": "MainTest.java",
                                  "content": "package workspace;import org.assertj.core.api.Assertions;import org.junit.jupiter.api.Test;public class MainTest {@Test void testOutput() {}}"
                                }
                              ],
                              "userId": "aae5a89c-cac0-4364-adb6-2392c12fcf62",
                              "testClassNames": ["MainTest"],
                              "buildTool": "gradle",
                              "mainClassName": "Main",
                              "type": "ATTEMPT"
                            }
                            """))
                    .check(status().is(200))
            )
            .pause(1);

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

    }
}
