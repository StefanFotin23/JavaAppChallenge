package com.example.app.ekyc.dossier.repository;

import com.example.app.ekyc.dossier.domain.Dossier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface DossierRepository extends JpaRepository<Dossier, Long> {
    Optional<Dossier> findByDossierNumber(String dossierNumber);

    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT d FROM Dossier d " +
            "LEFT JOIN FETCH d.parties p " +
            "LEFT JOIN FETCH p.address " +
            "LEFT JOIN FETCH p.sanction " +
            "LEFT JOIN FETCH p.sourceOfWealth " +
            "LEFT JOIN FETCH p.risk " +
            "WHERE d.id = :id")
    Optional<Dossier> findWithDetailsById(@org.springframework.data.repository.query.Param("id") Long id);
}
