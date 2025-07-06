import io.gatling.javaapi.core.Simulation;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.rampUsersPerSec;

public class StressTestSimulation extends Simulation {
    {
        setUp(
                StudentScenarios.scenario.injectOpen(
                        rampUsersPerSec(10).to(100).during(Duration.ofMinutes(3))
                )
        ).protocols(StudentScenarios.httpProtocol);
    }
}
