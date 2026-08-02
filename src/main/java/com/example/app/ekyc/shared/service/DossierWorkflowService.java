package com.example.app.ekyc.shared.service;

import com.example.app.ekyc.dossier.domain.Dossier;
import com.example.app.ekyc.dossier.repository.DossierRepository;
import com.example.app.ekyc.compliance.domain.DossierParty;
import com.example.app.ekyc.compliance.domain.DossierPartyAddress;
import com.example.app.ekyc.compliance.domain.DossierPartyRisk;
import com.example.app.ekyc.compliance.domain.DossierPartySourceOfWealth;
import com.example.app.ekyc.compliance.repository.DossierPartyRepository;
import com.example.app.ekyc.shared.util.RiskFieldComparator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DossierWorkflowService {

    private final DossierRepository dossierRepository;
    private final DossierPartyRepository dossierPartyRepository;
    private final RiskCalculationService riskCalculationService;
    private final org.springframework.cache.CacheManager cacheManager;

    @Transactional(readOnly = true)
    public Optional<Dossier> getDossier(Long id) {
        return dossierRepository.findById(id);
    }

    @Transactional(readOnly = true)
    @org.springframework.cache.annotation.Cacheable(value = "dossier", key = "#id", unless = "#result == null")
    public Optional<Dossier> getDossierWithDetails(Long id) {
        Optional<Dossier> dossierOpt = dossierRepository.findWithDetailsById(id);
        if (dossierOpt.isPresent()) {
            Dossier dossier = dossierOpt.get();
            // Force loading of lazy fields for simulation profiling purposes
            if (dossier.getParties() != null) {
                for (DossierParty party : dossier.getParties()) {
                    if (party.getAddress() != null) party.getAddress().getCity();
                    if (party.getSanction() != null) party.getSanction().getSanctionsCheckStatus();
                    if (party.getSourceOfWealth() != null) party.getSourceOfWealth().getSourceType();
                    if (party.getRisk() != null) party.getRisk().getScreeningRiskLevel();
                }
            }
        }
        return dossierOpt;
    }

    @Transactional
    public DossierParty updatePartyAddress(Long partyId, String street, String city, String country) {
        DossierParty party = dossierPartyRepository.findById(partyId)
                .orElseThrow(() -> new IllegalArgumentException("DossierParty not found: " + partyId));

        DossierPartyAddress oldAddress = party.getAddress();
        
        // Clone old address properties to check critical changes
        DossierPartyAddress snapshot = null;
        if (oldAddress != null) {
            snapshot = DossierPartyAddress.builder()
                    .country(oldAddress.getCountry())
                    .streetAddress(oldAddress.getStreetAddress())
                    .city(oldAddress.getCity())
                    .build();
        }

        if (oldAddress == null) {
            DossierPartyAddress newAddress = DossierPartyAddress.builder()
                    .dossierParty(party)
                    .streetAddress(street)
                    .city(city)
                    .country(country)
                    .build();
            party.setAddress(newAddress);
        } else {
            oldAddress.setStreetAddress(street);
            oldAddress.setCity(city);
            oldAddress.setCountry(country);
        }

        DossierParty savedParty = dossierPartyRepository.saveAndFlush(party);
        evictDossiersForParty(savedParty);

        // Check if critical field (country) changed
        if (snapshot == null || RiskFieldComparator.hasCriticalFieldChanged(snapshot, savedParty.getAddress())) {
            riskCalculationService.recalculatePartyRisk(partyId);
        }

        return savedParty;
    }

    @Transactional
    public DossierParty updatePartyRiskScore(Long partyId, Double score, String riskLevel, Boolean isPep) {
        DossierParty party = dossierPartyRepository.findById(partyId)
                .orElseThrow(() -> new IllegalArgumentException("DossierParty not found: " + partyId));

        DossierPartyRisk oldRisk = party.getRisk();
        
        DossierPartyRisk snapshot = null;
        if (oldRisk != null) {
            snapshot = DossierPartyRisk.builder()
                    .countryRiskScore(oldRisk.getCountryRiskScore())
                    .screeningRiskLevel(oldRisk.getScreeningRiskLevel())
                    .isPep(oldRisk.getIsPep())
                    .build();
        }

        if (oldRisk == null) {
            DossierPartyRisk newRisk = DossierPartyRisk.builder()
                    .dossierParty(party)
                    .countryRiskScore(score)
                    .screeningRiskLevel(riskLevel)
                    .isPep(isPep)
                    .build();
            party.setRisk(newRisk);
        } else {
            oldRisk.setCountryRiskScore(score);
            oldRisk.setScreeningRiskLevel(riskLevel);
            oldRisk.setIsPep(isPep);
        }

        DossierParty savedParty = dossierPartyRepository.saveAndFlush(party);
        evictDossiersForParty(savedParty);

        // Check if critical risk fields changed
        if (snapshot == null || RiskFieldComparator.hasCriticalFieldChanged(snapshot, savedParty.getRisk())) {
            riskCalculationService.recalculatePartyRisk(partyId);
        }

        return savedParty;
    }

    @Transactional
    public DossierParty updatePartySourceOfWealth(Long partyId, String sourceType, Double estimatedValueUsd, String verificationStatus) {
        DossierParty party = dossierPartyRepository.findById(partyId)
                .orElseThrow(() -> new IllegalArgumentException("DossierParty not found: " + partyId));

        DossierPartySourceOfWealth oldSow = party.getSourceOfWealth();
        
        DossierPartySourceOfWealth snapshot = null;
        if (oldSow != null) {
            snapshot = DossierPartySourceOfWealth.builder()
                    .estimatedValueUsd(oldSow.getEstimatedValueUsd())
                    .sourceType(oldSow.getSourceType())
                    .verificationStatus(oldSow.getVerificationStatus())
                    .build();
        }

        if (oldSow == null) {
            DossierPartySourceOfWealth newSow = DossierPartySourceOfWealth.builder()
                    .dossierParty(party)
                    .estimatedValueUsd(estimatedValueUsd)
                    .sourceType(sourceType)
                    .verificationStatus(verificationStatus)
                    .build();
            party.setSourceOfWealth(newSow);
        } else {
            oldSow.setEstimatedValueUsd(estimatedValueUsd);
            oldSow.setSourceType(sourceType);
            oldSow.setVerificationStatus(verificationStatus);
        }

        DossierParty savedParty = dossierPartyRepository.saveAndFlush(party);
        evictDossiersForParty(savedParty);

        // Check if critical value field changed
        if (snapshot == null || RiskFieldComparator.hasCriticalFieldChanged(snapshot, savedParty.getSourceOfWealth())) {
            riskCalculationService.recalculatePartyRisk(partyId);
        }

        return savedParty;
    }

    private void evictDossiersForParty(DossierParty party) {
        org.springframework.cache.Cache cache = cacheManager.getCache("dossier");
        if (cache != null && party.getDossiers() != null) {
            for (Dossier dossier : party.getDossiers()) {
                cache.evict(dossier.getId());
            }
        }
    }
}
