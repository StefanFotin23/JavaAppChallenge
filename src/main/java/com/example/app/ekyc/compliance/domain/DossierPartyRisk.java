package com.example.app.ekyc.compliance.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "t_dossier_party_risk")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DossierPartyRisk {

    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "dossier_party_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private DossierParty dossierParty;

    @com.example.app.ekyc.shared.annotation.TriggerRiskEvaluation
    @Column(name = "screening_risk_level", nullable = false, length = 20)
    private String screeningRiskLevel; // e.g. LOW, MEDIUM, HIGH

    @com.example.app.ekyc.shared.annotation.TriggerRiskEvaluation
    @Column(name = "country_risk_score", nullable = false)
    private Double countryRiskScore;

    @com.example.app.ekyc.shared.annotation.TriggerRiskEvaluation
    @Column(name = "pep_flag", nullable = false)
    private Boolean isPep;

    @Column(name = "last_updated", nullable = false)
    private Instant lastUpdated;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        this.lastUpdated = Instant.now();
    }
}
