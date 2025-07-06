import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.util.concurrent.atomic.AtomicInteger;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class StudentScenarios {

    public static final HttpProtocolBuilder httpProtocol = http
            .baseUrl("http://localhost:8080")
            .acceptHeader("application/json")
            .contentTypeHeader("application/json");
    private static final AtomicInteger studentIndex = new AtomicInteger(1);
    public static final ScenarioBuilder scenario = scenario("Student Full Flow: Login → Dashboard → Course → Submissions → Rubrics")

            .exec(session -> {
                int index = studentIndex.getAndIncrement();
                return session.set("studentEmail", "student2@example.com");
            })
            .exec(http("Student Login")
                    .post("/api/auth/login")
                    .body(StringBody(session -> String.format("""
                            {
                              "email": "%s",
                              "password": "student123"
                            }
                            """, session.getString("studentEmail"))))
                    .check(status().is(200))
                    .check(jsonPath("$.data.accessToken").exists().saveAs("authToken"))
            ).exitHereIfFailed()
            .pause(1)

            .exec(http("Access Dashboard")
                    .get("/api/dashboard")
                    .header("Authorization", session -> "Bearer " + session.getString("authToken"))
                    .check(status().is(200))
            ).pause(1)

            .exec(http("Get Courses")
                    .get("/api/courses")
                    .header("Authorization", session -> "Bearer " + session.getString("authToken"))
                    .check(status().is(200))
                    .check(jsonPath("$.data.content[0].id").saveAs("courseId"))
            ).exitHereIfFailed()
            .pause(1)

            .exec(http("Get Course Detail")
                    .get(session -> "/api/courses/" + session.getString("courseId"))
                    .header("Authorization", session -> "Bearer " + session.getString("authToken"))
                    .check(status().is(200))
                    .check(jsonPath("$.data.assignments[0].id").saveAs("assignmentId"))
            ).exitHereIfFailed()
            .pause(1)

            .exec(http("Get Submissions for Assignment")
                    .get(session -> String.format("/api/submissions?page=0&size=1000&filter=assignment=eq:%s",
                            session.getString("assignmentId")))
                    .header("Authorization", session -> "Bearer " + session.getString("authToken"))
                    .check(status().in(200, 204))
            ).pause(1);


}
