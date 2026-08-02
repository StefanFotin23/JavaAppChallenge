package com.example.app.ekyc.shared.service;

import com.example.app.ekyc.client.domain.Client;
import com.example.app.ekyc.client.domain.ClientDetails;
import com.example.app.ekyc.client.domain.ClientRisk;
import com.example.app.ekyc.client.repository.ClientRepository;
import com.example.app.ekyc.dossier.domain.Dossier;
import com.example.app.ekyc.dossier.repository.DossierRepository;
import com.example.app.ekyc.compliance.domain.*;
import com.example.app.ekyc.compliance.repository.DossierPartyRepository;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class EkycSimulationService {

    private final DossierRepository dossierRepository;
    private final ClientRepository clientRepository;
    private final DossierPartyRepository dossierPartyRepository;
    
    private final DossierWorkflowService workflowService;
    private final ValidationService validationService;
    private final RiskCalculationService riskCalculationService;

    private final List<Long> initializedDossierIds = new ArrayList<>();

    @Getter
    @Builder
    public static class SimulationResult {
        private int totalActions;
        private int totalGets;
        private int totalUpdates;
        private int totalValidations;
        private long criticalRiskCalculationsCount;
        private long totalDurationMs;
    }

    /**
     * Seed database with test dossiers, related clients, and parties.
     */
    public synchronized void generateMockData(int numDossiers) {
        log.info("Generating {} mock dossiers for eKYC simulation...", numDossiers);
        initializedDossierIds.clear();
        Random rand = new Random();

        for (int i = 0; i < numDossiers; i++) {
            Dossier dossier = Dossier.builder()
                    .dossierNumber("DOS-" + UUID.randomUUID().toString().substring(0, 8))
                    .status("IN_PROGRESS")
                    .build();

            int numClients = rand.nextInt(3) + 1; // 1 to 3 clients
            for (int c = 0; c < numClients; c++) {
                Client client = Client.builder()
                        .clientNumber("CL-" + UUID.randomUUID().toString().substring(0, 8))
                        .clientType(rand.nextBoolean() ? "INDIVIDUAL" : "CORPORATE")
                        .build();

                client.setClientDetails(ClientDetails.builder()
                        .client(client)
                        .legalName("Client Legal Name " + i + "-" + c)
                        .countryOfResidence("Switzerland")
                        .dateOfBirth(LocalDate.of(1980 + rand.nextInt(20), 1 + rand.nextInt(11), 1 + rand.nextInt(28)))
                        .industrySector("FINANCE")
                        .build());

                client.setClientRisk(ClientRisk.builder()
                        .client(client)
                        .riskRating("MEDIUM")
                        .score(0.5)
                        .lastEvaluationDate(Instant.now())
                        .isPep(false)
                        .build());

                int numParties = rand.nextInt(3) + 2; // 2 to 4 parties
                for (int p = 0; p < numParties; p++) {
                    DossierParty party = DossierParty.builder()
                            .partyName("Party Name " + i + "-" + c + "-" + p)
                            .partyType("UBO")
                            .build();

                    party.setAddress(DossierPartyAddress.builder()
                            .dossierParty(party)
                            .streetAddress("100 Zurich St")
                            .city("Zurich")
                            .postalCode("8001")
                            .country("Switzerland")
                            .build());

                    party.setSanction(DossierPartySanction.builder()
                            .dossierParty(party)
                            .sanctionsCheckStatus("PASSED")
                            .hitDetails("Clean profile")
                            .build());

                    party.setSourceOfWealth(DossierPartySourceOfWealth.builder()
                            .dossierParty(party)
                            .sourceType("SALARY")
                            .estimatedValueUsd(2000000.0)
                            .verificationStatus("VERIFIED")
                            .build());

                    party.setRisk(DossierPartyRisk.builder()
                            .dossierParty(party)
                            .screeningRiskLevel("LOW")
                            .countryRiskScore(0.15)
                            .isPep(false)
                            .build());

                    dossier.getParties().add(party);
                }
            }

            Dossier savedDossier = dossierRepository.save(dossier);
            initializedDossierIds.add(savedDossier.getId());
        }
        log.info("Mock database generation complete. Total dossiers: {}", initializedDossierIds.size());
    }

    /**
     * Simulates concurrent user actions on dossiers.
     */
    public SimulationResult runSimulation(int numUsers, int actionsPerUser) throws Exception {
        if (initializedDossierIds.isEmpty()) {
            throw new IllegalStateException("Simulation database is empty! Please generate mock data first.");
        }

        log.info("Starting eKYC flow simulation with {} concurrent users, {} actions/user...", numUsers, actionsPerUser);
        riskCalculationService.reset();
        
        ExecutorService executor = Executors.newFixedThreadPool(numUsers);
        List<Callable<Map<String, Integer>>> tasks = new ArrayList<>();
        
        long startTime = System.currentTimeMillis();

        for (int u = 0; u < numUsers; u++) {
            tasks.add(() -> {
                Random rand = new Random();
                int gets = 0;
                int updates = 0;
                int validations = 0;
                
                // Select a random dossier for this simulated user session
                Long dossierId = initializedDossierIds.get(rand.nextInt(initializedDossierIds.size()));

                for (int a = 0; a < actionsPerUser; a++) {
                    int choice = rand.nextInt(10);
                    if (choice < 5) {
                        // 50% chance: GET dossier details (Home page loads details)
                        Optional<Dossier> docOpt = workflowService.getDossierWithDetails(dossierId);
                        gets++;
                        
                        // User triggers validation on loading page
                        if (docOpt.isPresent()) {
                            validationService.validateDossier(docOpt.get());
                            validations++;
                        }
                    } else if (choice < 8) {
                        // 30% chance: Non-critical field updates (address street/city)
                        Optional<Dossier> docOpt = workflowService.getDossierWithDetails(dossierId);
                        if (docOpt.isPresent() && !docOpt.get().getParties().isEmpty()) {
                            DossierParty party = docOpt.get().getParties().iterator().next();
                            workflowService.updatePartyAddress(
                                    party.getId(),
                                    "New Street " + rand.nextInt(1000),
                                    "New City " + rand.nextInt(100),
                                    party.getAddress() != null ? party.getAddress().getCountry() : "Switzerland"
                            );
                            updates++;
                        }
                    } else {
                        // 20% chance: Critical field updates (change country of residence/nationality, change PEP status)
                        Optional<Dossier> docOpt = workflowService.getDossierWithDetails(dossierId);
                        if (docOpt.isPresent() && !docOpt.get().getParties().isEmpty()) {
                            DossierParty party = docOpt.get().getParties().iterator().next();
                            
                            // Alternate critical changes
                            if (rand.nextBoolean()) {
                                workflowService.updatePartyAddress(
                                        party.getId(),
                                        party.getAddress() != null ? party.getAddress().getStreetAddress() : "100 Zurich St",
                                        party.getAddress() != null ? party.getAddress().getCity() : "Zurich",
                                        rand.nextBoolean() ? "Cayman Islands" : "Switzerland"
                                );
                            } else {
                                workflowService.updatePartyRiskScore(
                                        party.getId(),
                                        0.95,
                                        "HIGH",
                                        true
                                );
                            }
                            updates++;
                        }
                    }
                }

                Map<String, Integer> counts = new HashMap<>();
                counts.put("gets", gets);
                counts.put("updates", updates);
                counts.put("validations", validations);
                return counts;
            });
        }

        List<Future<Map<String, Integer>>> futures = executor.invokeAll(tasks);
        executor.shutdown();

        int totalGets = 0;
        int totalUpdates = 0;
        int totalValidations = 0;

        for (Future<Map<String, Integer>> future : futures) {
            Map<String, Integer> res = future.get();
            totalGets += res.getOrDefault("gets", 0);
            totalUpdates += res.getOrDefault("updates", 0);
            totalValidations += res.getOrDefault("validations", 0);
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        long calculationsCount = riskCalculationService.getRecalculationCount();

        log.info("Simulation completed in {} ms.", duration);
        log.info("GET page loads: {}, Field updates: {}, Validation triggers: {}", totalGets, totalUpdates, totalValidations);
        log.info("Critical Risk Recalculations triggered: {}", calculationsCount);

        return SimulationResult.builder()
                .totalActions(totalGets + totalUpdates)
                .totalGets(totalGets)
                .totalUpdates(totalUpdates)
                .totalValidations(totalValidations)
                .criticalRiskCalculationsCount(calculationsCount)
                .totalDurationMs(duration)
                .build();
    }
}
