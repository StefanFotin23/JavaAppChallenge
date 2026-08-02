package com.example.app.ekyc.dossier.domain;

import com.example.app.ekyc.compliance.domain.DossierParty;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "t_dossier")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dossier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dossier_number", nullable = false, unique = true, length = 50)
    private String dossierNumber;

    @Column(name = "status", nullable = false, length = 30)
    private String status; // e.g. PENDING, APPROVED, REJECTED, SUSPENDED

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "t_dossier_dossier_party",
        joinColumns = @JoinColumn(name = "dossier_id"),
        inverseJoinColumns = @JoinColumn(name = "dossier_party_id")
    )
    @Builder.Default
    private Set<DossierParty> parties = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}
