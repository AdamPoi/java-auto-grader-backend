import io.gatling.core.Predef.*
import io.gatling.http.Predef.*

import scala.concurrent.duration.*

class AutoGraderSimulation extends Simulation {

  // 1. Define the HTTP protocol configuration
  val httpProtocol = http
    .baseUrl("http://localhost:8080/api") // Base URL for all requests
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")

  // 2. Define the data feeder for user credentials
  val userFeeder = csv("users.csv").random // Use random users from the csv

  // 3. Define the main scenario
  val userJourney = scenario("Authenticated User Journey")
    .feed(userFeeder) // Inject data from the CSV file

    // Step 1: Login and save the token
    .exec(
      http("POST /auth/login")
        .post("/auth/login")
        .body(StringBody("""{ "email": "${username}", "password": "${password}" }""")).asJson
        .check(status.is(200))
        .check(jsonPath("$.data.accessToken").saveAs("jwt_token")) // Extract and save the token
    )
    .pause(1.second, 3.seconds) // Simulate user think time

    // Step 2: Use the token to get a list of courses
    .exec(
      http("GET /courses")
        .get("/courses")
        .header("Authorization", "Bearer ${jwt_token}") // Use the saved token
        .check(status.is(200))
        .check(jsonPath("$.data.content[0].id").saveAs("courseId")) // Save the first course ID
    )
    .pause(2.seconds, 5.seconds)

    // Step 3: Use the token and courseId to get assignments
    .exec(
      http("GET /courses/{courseId}/assignments")
        .get("/courses/${courseId}/assignments")
        .header("Authorization", "Bearer ${jwt_token}")
        .check(status.is(200))
    )

  // 4. Define the load simulation
  setUp(
    userJourney.inject(
      nothingFor(4.seconds), // 1
      atOnceUsers(10), // 2
      rampUsers(10).during(5.seconds), // 3
      constantUsersPerSec(20).during(15.seconds), // 4
      constantUsersPerSec(20).during(15.seconds).randomized, // 5
      rampUsersPerSec(10).to(20).during(10.minutes), // 6
      rampUsersPerSec(10).to(20).during(10.minutes).randomized, // 7
      stressPeakUsers(1000).during(20.seconds) // 8
    ).protocols(httpProtocol)
  )
}
