package com.example.app.ekyc.shared.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
public class RiskCalculationService {

    private final AtomicLong recalculationCount = new AtomicLong(0);

    /**
     * Simulates performing PEP screenings, sanction matching checks, and score aggregations.
     */
    public void recalculatePartyRisk(Long partyId) {
        recalculationCount.incrementAndGet();
        log.debug("Triggered compliance risk recalculation for party [{}]", partyId);
        // Simulating standard CPU-bound enterprise logic calculation time
        try {
            Thread.sleep(5); 
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public long getRecalculationCount() {
        return recalculationCount.get();
    }

    public void reset() {
        recalculationCount.set(0);
    }
}
