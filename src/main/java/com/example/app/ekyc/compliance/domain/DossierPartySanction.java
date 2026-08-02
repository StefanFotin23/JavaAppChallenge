package com.example.app.ekyc.compliance.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "t_dossier_party_sanction")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DossierPartySanction {

    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "dossier_party_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private DossierParty dossierParty;

    @Column(name = "sanctions_check_status", nullable = false, length = 30)
    private String sanctionsCheckStatus; // e.g. PASSED, MATCH_FOUND, FALSE_POSITIVE

    @Column(name = "hit_details", columnDefinition = "TEXT")
    private String hitDetails;

    @Column(name = "performed_at", nullable = false)
    private Instant performedAt;

    @PrePersist
    protected void onCreate() {
        this.performedAt = Instant.now();
    }
}
