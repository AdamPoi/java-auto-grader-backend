import io.gatling.javaapi.core.Simulation;

import static io.gatling.javaapi.core.CoreDsl.atOnceUsers;

public class ScenarioTestSimulation extends Simulation {
    {
        setUp(
                CompilerScenarios.scenario.injectOpen(
                        atOnceUsers(10)
                )
        ).protocols(CompilerScenarios.httpProtocol);
    }
}
