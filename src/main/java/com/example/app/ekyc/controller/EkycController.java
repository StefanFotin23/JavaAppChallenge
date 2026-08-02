package com.example.app.ekyc.controller;

import com.example.app.ekyc.compliance.domain.DossierParty;
import com.example.app.ekyc.dossier.domain.Dossier;
import com.example.app.ekyc.shared.service.DossierWorkflowService;
import com.example.app.ekyc.shared.service.EkycSimulationService;
import com.example.app.ekyc.shared.service.RiskCalculationService;
import com.example.app.ekyc.shared.service.ValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/ekyc")
@RequiredArgsConstructor
public class EkycController {

    private final DossierWorkflowService workflowService;
    private final EkycSimulationService simulationService;
    private final ValidationService validationService;
    private final RiskCalculationService riskCalculationService;
    private final com.example.app.ekyc.dossier.repository.DossierRepository dossierRepository;
    private final com.example.app.ekyc.shared.profiling.ProfilingRegistry profilingRegistry;
    private final jakarta.persistence.EntityManagerFactory entityManagerFactory;

    @GetMapping("/dossiers/ids")
    public ResponseEntity<java.util.List<Long>> getAllDossierIds() {
        return ResponseEntity.ok(dossierRepository.findAll().stream()
                .map(com.example.app.ekyc.dossier.domain.Dossier::getId)
                .toList());
    }

    @PostMapping("/simulation/generate")
    public ResponseEntity<Map<String, Object>> generateMockData(@RequestParam(defaultValue = "100") int numDossiers) {
        simulationService.generateMockData(numDossiers);
        return ResponseEntity.ok(Map.of("message", "Generated " + numDossiers + " mock dossiers successfully"));
    }

    @GetMapping("/dossiers/{id}")
    public ResponseEntity<Dossier> getDossier(@PathVariable Long id) {
        return workflowService.getDossierWithDetails(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/dossiers/{id}/validate")
    public ResponseEntity<Map<String, Object>> validateDossier(@PathVariable Long id) {
        Optional<Dossier> docOpt = workflowService.getDossierWithDetails(id);
        if (docOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        boolean isValid = validationService.validateDossier(docOpt.get());
        return ResponseEntity.ok(Map.of("dossierId", id, "isValid", isValid));
    }

    @PutMapping("/dossiers/parties/{partyId}/address")
    public ResponseEntity<DossierParty> updatePartyAddress(
            @PathVariable Long partyId,
            @RequestParam String street,
            @RequestParam String city,
            @RequestParam String country) {
        try {
            DossierParty updated = workflowService.updatePartyAddress(partyId, street, city, country);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/dossiers/parties/{partyId}/risk")
    public ResponseEntity<DossierParty> updatePartyRisk(
            @PathVariable Long partyId,
            @RequestParam Double score,
            @RequestParam String riskLevel,
            @RequestParam Boolean isPep) {
        try {
            DossierParty updated = workflowService.updatePartyRiskScore(partyId, score, riskLevel, isPep);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/simulation/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        org.hibernate.SessionFactory sessionFactory = entityManagerFactory.unwrap(org.hibernate.SessionFactory.class);
        org.hibernate.stat.Statistics hibernateStats = sessionFactory.getStatistics();

        java.util.Map<String, Object> methodMetrics = new java.util.HashMap<>();
        profilingRegistry.getStats().forEach((method, stats) -> {
            methodMetrics.put(method, java.util.Map.of(
                "callCount", stats.getCallCountVal(),
                "avgDurationMs", stats.getAvgDurationMs()
            ));
        });

        return ResponseEntity.ok(java.util.Map.of(
                "riskRecalculationsCount", riskCalculationService.getRecalculationCount(),
                "methodMetrics", methodMetrics,
                "databaseMetrics", java.util.Map.of(
                        "connectionsCount", hibernateStats.getConnectCount(),
                        "statementsPrepared", hibernateStats.getPrepareStatementCount(),
                        "entitiesLoaded", hibernateStats.getEntityLoadCount(),
                        "entitiesInserted", hibernateStats.getEntityInsertCount(),
                        "entitiesUpdated", hibernateStats.getEntityUpdateCount(),
                        "collectionsLoaded", hibernateStats.getCollectionLoadCount()
                )
        ));
    }

    @PostMapping("/simulation/stats/reset")
    public ResponseEntity<Void> resetStats() {
        riskCalculationService.reset();
        profilingRegistry.reset();
        org.hibernate.SessionFactory sessionFactory = entityManagerFactory.unwrap(org.hibernate.SessionFactory.class);
        sessionFactory.getStatistics().clear();
        return ResponseEntity.ok().build();
    }
}
