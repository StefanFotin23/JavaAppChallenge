package com.example.app.ekyc.association.domain;

import com.example.app.ekyc.client.domain.Client;
import com.example.app.ekyc.compliance.domain.DossierParty;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "t_client_party_association", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"client_id", "dossier_party_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientPartyAssociation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties("clientPartyAssociations")
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dossier_party_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private DossierParty dossierParty;

    @Column(name = "association_role", nullable = false, length = 50)
    private String associationRole; // e.g. UBO, REPRESENTATIVE, SHAREHOLDER, TRUSTEE

    @Column(name = "associated_at", nullable = false)
    private Instant associatedAt;

    @PrePersist
    protected void onCreate() {
        this.associatedAt = Instant.now();
    }
}
