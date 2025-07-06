import io.gatling.javaapi.core.Simulation;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.constantUsersPerSec;
import static io.gatling.javaapi.core.CoreDsl.rampUsersPerSec;

public class LoadTestSimulation extends Simulation {
    {
        setUp(
                StudentScenarios.scenario.injectOpen(
                        rampUsersPerSec(1).to(20).during(Duration.ofMinutes(1)),
                        constantUsersPerSec(20).during(Duration.ofMinutes(3))
                )
        ).protocols(StudentScenarios.httpProtocol);
    }
}
