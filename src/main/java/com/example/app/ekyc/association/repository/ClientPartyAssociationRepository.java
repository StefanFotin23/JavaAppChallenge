package com.example.app.ekyc.association.repository;

import com.example.app.ekyc.association.domain.ClientPartyAssociation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ClientPartyAssociationRepository extends JpaRepository<ClientPartyAssociation, Long> {
    List<ClientPartyAssociation> findByClientId(Long clientId);
    List<ClientPartyAssociation> findByDossierPartyId(Long dossierPartyId);
}
