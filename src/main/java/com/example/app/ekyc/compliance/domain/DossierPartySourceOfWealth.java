package com.example.app.ekyc.compliance.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "t_dossier_party_source_of_wealth")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DossierPartySourceOfWealth {

    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "dossier_party_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private DossierParty dossierParty;

    @Column(name = "source_type", nullable = false, length = 50)
    private String sourceType; // e.g. INHERITANCE, SALARY, BUSINESS_PROCEEDS

    @com.example.app.ekyc.shared.annotation.TriggerRiskEvaluation
    @Column(name = "estimated_value_usd", nullable = false)
    private Double estimatedValueUsd;

    @Column(name = "verification_status", nullable = false, length = 30)
    private String verificationStatus; // e.g. VERIFIED, UNVERIFIED, WAITING_DOCUMENTATION
}
