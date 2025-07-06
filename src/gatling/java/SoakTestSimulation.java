import io.gatling.javaapi.core.Simulation;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.constantUsersPerSec;

public class SoakTestSimulation extends Simulation {
    {
        setUp(
                StudentScenarios.scenario.injectOpen(
                        constantUsersPerSec(15).during(Duration.ofMinutes(10))
                )
        ).protocols(StudentScenarios.httpProtocol);
    }
}
