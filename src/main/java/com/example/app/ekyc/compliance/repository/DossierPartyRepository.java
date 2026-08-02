package com.example.app.ekyc.compliance.repository;

import com.example.app.ekyc.compliance.domain.DossierParty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DossierPartyRepository extends JpaRepository<DossierParty, Long> {
}
