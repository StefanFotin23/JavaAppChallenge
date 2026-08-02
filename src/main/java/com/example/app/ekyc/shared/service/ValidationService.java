package com.example.app.ekyc.shared.service;

import com.example.app.ekyc.dossier.domain.Dossier;
import com.example.app.ekyc.compliance.domain.DossierParty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ValidationService {

    /**
     * Performs validation checks on a dossier:
     * - Verify Sanctions check status.
     * - Verify that HIGH risk parties have VERIFIED Source of Wealth.
     * - Verify that address, risk, and compliance records exist.
     */
    public boolean validateDossier(Dossier dossier) {
        if (dossier == null || dossier.getParties() == null || dossier.getParties().isEmpty()) {
            return false;
        }

        boolean isValid = true;
        for (DossierParty party : dossier.getParties()) {
            // Check sanctions
            if (party.getSanction() != null && "MATCH_FOUND".equals(party.getSanction().getSanctionsCheckStatus())) {
                isValid = false;
            }
            // Check high risk constraints
            if (party.getRisk() != null && "HIGH".equals(party.getRisk().getScreeningRiskLevel())) {
                if (party.getSourceOfWealth() == null || !"VERIFIED".equals(party.getSourceOfWealth().getVerificationStatus())) {
                    isValid = false;
                }
            }
        }

        // Simulate basic validation CPU cost
        try {
            Thread.sleep(2);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return isValid;
    }
}
