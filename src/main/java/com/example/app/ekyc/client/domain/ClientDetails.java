package com.example.app.ekyc.client.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "t_client_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientDetails {

    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "client_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Client client;

    @Column(name = "legal_name", nullable = false, length = 255)
    private String legalName;

    @com.example.app.ekyc.shared.annotation.TriggerRiskEvaluation
    @Column(name = "country_of_incorporation_or_residence", nullable = false, length = 100)
    private String countryOfResidence;

    @Column(name = "date_of_incorporation_or_birth")
    private LocalDate dateOfBirth;

    @Column(name = "industry_sector", length = 100)
    private String industrySector;
}
