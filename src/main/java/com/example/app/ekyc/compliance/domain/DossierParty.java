package com.example.app.ekyc.compliance.domain;

import com.example.app.ekyc.association.domain.ClientPartyAssociation;
import com.example.app.ekyc.dossier.domain.Dossier;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "t_dossier_party")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DossierParty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "party_name", nullable = false, length = 255)
    private String partyName;

    @Column(name = "party_type", nullable = false, length = 50)
    private String partyType; // e.g. SHAREHOLDER, TRUSTEE, DIRECTOR, CLIENT

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @ManyToMany(mappedBy = "parties")
    @com.fasterxml.jackson.annotation.JsonIgnore
    @Builder.Default
    private Set<Dossier> dossiers = new HashSet<>();

    @OneToOne(mappedBy = "dossierParty", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private DossierPartyAddress address;

    @OneToOne(mappedBy = "dossierParty", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private DossierPartySanction sanction;

    @OneToOne(mappedBy = "dossierParty", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private DossierPartySourceOfWealth sourceOfWealth;

    @OneToOne(mappedBy = "dossierParty", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private DossierPartyRisk risk;

    @OneToMany(mappedBy = "dossierParty", cascade = CascadeType.ALL, orphanRemoval = true)
    @com.fasterxml.jackson.annotation.JsonIgnore
    @Builder.Default
    private List<ClientPartyAssociation> clientPartyAssociations = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}
