package com.example.app.ekyc;

import com.example.app.ekyc.client.domain.*;
import com.example.app.ekyc.client.repository.*;
import com.example.app.ekyc.dossier.domain.*;
import com.example.app.ekyc.dossier.repository.*;
import com.example.app.ekyc.compliance.domain.*;
import com.example.app.ekyc.compliance.repository.*;
import com.example.app.ekyc.association.domain.*;
import com.example.app.ekyc.association.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class EkycDomainIntegrationTest {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private DossierRepository dossierRepository;

    @Autowired
    private DossierPartyRepository dossierPartyRepository;

    @Autowired
    private ClientPartyAssociationRepository associationRepository;

    @Test
    @DisplayName("Should successfully persist Client with Details and Risk rating")
    void shouldPersistClientWithDetailsAndRisk() {
        Client client = Client.builder()
                .clientNumber("CL-992384")
                .clientType("INDIVIDUAL")
                .build();

        ClientDetails details = ClientDetails.builder()
                .client(client)
                .legalName("Stefan Fotin")
                .countryOfResidence("Romania")
                .dateOfBirth(LocalDate.of(1995, 8, 23))
                .industrySector("FINTECH")
                .build();
        client.setClientDetails(details);

        ClientRisk risk = ClientRisk.builder()
                .client(client)
                .riskRating("HIGH")
                .score(0.88)
                .lastEvaluationDate(Instant.now())
                .isPep(false)
                .build();
        client.setClientRisk(risk);

        Client saved = clientRepository.save(client);
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getClientDetails()).isNotNull();
        assertThat(saved.getClientDetails().getLegalName()).isEqualTo("Stefan Fotin");
        assertThat(saved.getClientRisk().getRiskRating()).isEqualTo("HIGH");
    }

    @Test
    @DisplayName("Should establish a Many-To-Many relationship between Dossier and DossierParty")
    void shouldEstablishManyToManyDossierAndParty() {
        Dossier dossier1 = Dossier.builder()
                .dossierNumber("DOS-2026-001")
                .status("IN_PROGRESS")
                .build();

        Dossier dossier2 = Dossier.builder()
                .dossierNumber("DOS-2026-002")
                .status("PENDING")
                .build();

        DossierParty party1 = DossierParty.builder()
                .partyName("UBO Holding Ltd")
                .partyType("CORPORATE_SHAREHOLDER")
                .build();

        DossierParty party2 = DossierParty.builder()
                .partyName("Stefan Fotin Representative")
                .partyType("LEGAL_REPRESENTATIVE")
                .build();

        dossier1.getParties().add(party1);
        dossier1.getParties().add(party2);
        dossier2.getParties().add(party1);

        dossierRepository.save(dossier1);
        dossierRepository.save(dossier2);

        Optional<Dossier> fetchedDossier1 = dossierRepository.findByDossierNumber("DOS-2026-001");
        assertThat(fetchedDossier1).isPresent();
        assertThat(fetchedDossier1.get().getParties()).hasSize(2);

        Optional<Dossier> fetchedDossier2 = dossierRepository.findByDossierNumber("DOS-2026-002");
        assertThat(fetchedDossier2).isPresent();
        assertThat(fetchedDossier2.get().getParties()).hasSize(1);
    }

    @Test
    @DisplayName("Should link Client and DossierParty via ClientPartyAssociation with details")
    void shouldLinkClientAndDossierParty() {
        Client client = clientRepository.save(Client.builder()
                .clientNumber("CL-773")
                .clientType("CORPORATE")
                .build());

        DossierParty party = dossierPartyRepository.save(DossierParty.builder()
                .partyName("Stefan Fotin Advisor")
                .partyType("INVESTMENT_ADVISOR")
                .build());

        ClientPartyAssociation association = ClientPartyAssociation.builder()
                .client(client)
                .dossierParty(party)
                .associationRole("PRIMARY_ADVISOR")
                .build();

        associationRepository.save(association);

        List<ClientPartyAssociation> clientAssocs = associationRepository.findByClientId(client.getId());
        assertThat(clientAssocs).hasSize(1);
        assertThat(clientAssocs.get(0).getAssociationRole()).isEqualTo("PRIMARY_ADVISOR");
        assertThat(clientAssocs.get(0).getDossierParty().getPartyName()).isEqualTo("Stefan Fotin Advisor");
    }

    @Test
    @DisplayName("Should persist subsequent detail tables (Address, Sanctions, SourceOfWealth, Risk) linked to DossierParty")
    void shouldPersistSubsequentDetailsForDossierParty() {
        DossierParty party = DossierParty.builder()
                .partyName("Alex Mercer")
                .partyType("UBO")
                .build();

        DossierPartyAddress address = DossierPartyAddress.builder()
                .dossierParty(party)
                .streetAddress("123 Wealth St")
                .city("Zürich")
                .country("Switzerland")
                .build();
        party.setAddress(address);

        DossierPartySanction sanction = DossierPartySanction.builder()
                .dossierParty(party)
                .sanctionsCheckStatus("PASSED")
                .hitDetails("Checked against OFAC & EU list. No hits.")
                .build();
        party.setSanction(sanction);

        DossierPartySourceOfWealth sourceOfWealth = DossierPartySourceOfWealth.builder()
                .dossierParty(party)
                .sourceType("BUSINESS_PROCEEDS")
                .estimatedValueUsd(15000000.0)
                .verificationStatus("VERIFIED")
                .build();
        party.setSourceOfWealth(sourceOfWealth);

        DossierPartyRisk risk = DossierPartyRisk.builder()
                .dossierParty(party)
                .screeningRiskLevel("LOW")
                .countryRiskScore(0.12)
                .isPep(false)
                .build();
        party.setRisk(risk);

        DossierParty saved = dossierPartyRepository.save(party);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getAddress()).isNotNull();
        assertThat(saved.getAddress().getCity()).isEqualTo("Zürich");
        assertThat(saved.getSanction().getSanctionsCheckStatus()).isEqualTo("PASSED");
        assertThat(saved.getSourceOfWealth().getSourceType()).isEqualTo("BUSINESS_PROCEEDS");
        assertThat(saved.getRisk().getScreeningRiskLevel()).isEqualTo("LOW");
    }
}
