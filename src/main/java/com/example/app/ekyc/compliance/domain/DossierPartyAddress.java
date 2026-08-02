package com.example.app.ekyc.compliance.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "t_dossier_party_address")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DossierPartyAddress {

    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "dossier_party_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private DossierParty dossierParty;

    @Column(name = "street_address", nullable = false, length = 255)
    private String streetAddress;

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @com.example.app.ekyc.shared.annotation.TriggerRiskEvaluation
    @Column(name = "country", nullable = false, length = 100)
    private String country;
}
