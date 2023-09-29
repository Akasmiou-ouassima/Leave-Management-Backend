package com.nttdata.sid.repositories;


import com.nttdata.sid.entities.Utilisateur;
import com.nttdata.sid.enums.UserType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur,Long> {
    @Query("SELECT u FROM Utilisateur u WHERE u.nom like :kw")
    List<Utilisateur> findUtilisateurByNomIsContainingIgnoreCase(@Param("kw") String keyword);
    Optional<Utilisateur> findUtilisateurByEmail(String email);

    @Query("SELECT u FROM Utilisateur u WHERE u.equipe.id = :equipeId")
    List<Utilisateur> findUtilisateursByEquipeId(@Param("equipeId") Long equipeId);

    List<Utilisateur> findUtilisateursByType(UserType userType);

}
