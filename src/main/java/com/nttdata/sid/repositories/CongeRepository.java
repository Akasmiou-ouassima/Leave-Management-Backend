package com.nttdata.sid.repositories;


import com.nttdata.sid.entities.Conge;
import com.nttdata.sid.enums.Etat;
import com.nttdata.sid.enums.Type;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface CongeRepository extends JpaRepository<Conge,Long> {
    @Query("SELECT c FROM Conge c WHERE c.utilisateur.id = :userId AND (c.motif LIKE %:keyword% OR CAST(c.dateDebut AS string) LIKE %:keyword% OR CAST(c.dateFin AS string) LIKE %:keyword% OR CAST(c.etat AS string) LIKE %:keyword% OR CAST(c.type AS string) LIKE %:keyword%)")
    List<Conge> findCongeByUserAndKeyword(Long userId,@Param("keyword") String keyword);


    @Query("select c from Conge c where c.utilisateur.id = :id")
    List<Conge> findCongeByUtilisateur(@Param("id") Long utilisateurId);

    List<Conge> findCongesByUtilisateurId(Long utilisateur);


    List<Conge> findCongesByEtat(Etat etat);
}
