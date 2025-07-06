import io.gatling.javaapi.core.Simulation;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.*;

public class SpikeTestSimulation extends Simulation {
    {
        setUp(
                CompilerScenarios.scenario.injectOpen(
                        atOnceUsers(200),
                        nothingFor(Duration.ofSeconds(30)),
                        atOnceUsers(300),
                        nothingFor(Duration.ofSeconds(30)),
                        rampUsersPerSec(0).to(100).during(Duration.ofSeconds(10))
                )
        ).protocols(CompilerScenarios.httpProtocol);
    }
}
