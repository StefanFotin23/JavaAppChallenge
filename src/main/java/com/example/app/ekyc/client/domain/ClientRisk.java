package com.example.app.ekyc.client.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "t_client_risk")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientRisk {

    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "client_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Client client;

    @Column(name = "risk_rating", nullable = false, length = 20)
    private String riskRating; // e.g. LOW, MEDIUM, HIGH

    @Column(name = "score", nullable = false)
    private Double score; // e.g. 0.85

    @Column(name = "last_evaluation_date", nullable = false)
    private Instant lastEvaluationDate;

    @Column(name = "pep_status", nullable = false)
    private Boolean isPep; // Politically Exposed Person
}
