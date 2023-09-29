package com.nttdata.sid.repositories;

import com.nttdata.sid.entities.Conge;
import com.nttdata.sid.entities.Equipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EquipeRepository extends JpaRepository<Equipe,Long> {


    Optional<Equipe> findEquipeByNom(String nom);

    @Query("SELECT e FROM Equipe e WHERE e.utilisateur.id = :utilisateurId")
    List<Equipe> findEquipesByUtilisateurId(@Param("utilisateurId") Long id);

}
