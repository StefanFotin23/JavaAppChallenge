package com.example.app.ekyc;

import com.example.app.ekyc.shared.service.EkycSimulationService;
import com.example.app.ekyc.shared.service.EkycSimulationService.SimulationResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
    "jwt.secret=defaultSecretKeyForTestingStarWarsAppWhichIsLongEnoughForSha256Signature"
})
class EkycSimulationIntegrationTest {

    @Autowired
    private EkycSimulationService simulationService;

    @Test
    @DisplayName("Should populate mock data and execute concurrent eKYC workflow simulation")
    void testEkycWorkflowSimulation() throws Exception {
        // Generate mock data for 10 dossiers (containing multiple clients & parties)
        simulationService.generateMockData(10);

        // Run simulation with 5 concurrent users doing 20 actions each
        SimulationResult result = simulationService.runSimulation(5, 20);

        assertThat(result.getTotalGets()).isGreaterThan(0);
        assertThat(result.getTotalUpdates()).isGreaterThan(0);
        assertThat(result.getTotalValidations()).isGreaterThan(0);
        assertThat(result.getTotalDurationMs()).isGreaterThan(0);
        
        // Confirm that the custom @TriggerRiskEvaluation annotation successfully selective-triggers evaluations
        assertThat(result.getCriticalRiskCalculationsCount()).isGreaterThan(0);

        // Print performance stats for mentoring profiling discussion
        System.out.println("====== eKYC Action Flow Profile Statistics ======");
        System.out.println("Total Simulated Users: 5");
        System.out.println("GET Page Requests    : " + result.getTotalGets());
        System.out.println("PUT/POST Updates     : " + result.getTotalUpdates());
        System.out.println("Validation Checks    : " + result.getTotalValidations());
        System.out.println("Risk Evaluations     : " + result.getCriticalRiskCalculationsCount());
        System.out.println("Total Execution Time : " + result.getTotalDurationMs() + " ms");
        System.out.println("=================================================");
    }
}
