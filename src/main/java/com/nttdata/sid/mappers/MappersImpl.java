package com.nttdata.sid.mappers;

import com.nttdata.sid.dtos.CongeDTO;
import com.nttdata.sid.dtos.EquipeDTO;
import com.nttdata.sid.dtos.UtilisateurDTO;
import com.nttdata.sid.entities.Conge;
import com.nttdata.sid.entities.Equipe;
import com.nttdata.sid.entities.Utilisateur;
import com.nttdata.sid.enums.Type;
import com.nttdata.sid.enums.UserType;
import com.nttdata.sid.services.CongeServiceImpl;
import com.nttdata.sid.services.UtilisateurServiceImpl;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@Transactional
public class MappersImpl {
    public CongeDTO fromConge(Conge conge){
        CongeDTO congeDTO = new CongeDTO();
        BeanUtils.copyProperties(conge, congeDTO);
        congeDTO.setUtilisateurId(conge.getUtilisateur().getId());
        return congeDTO;
    }
    public Conge fromCongeDTO(CongeDTO congeDTO){
        Conge conge = new Conge();
        BeanUtils.copyProperties(congeDTO, conge);
        return conge;
    }
    public EquipeDTO fromEquipe(Equipe equipe){
        EquipeDTO equipeDTO = new EquipeDTO();
        BeanUtils.copyProperties(equipe, equipeDTO);
        equipeDTO.setResponsableId(equipe.getUtilisateur().getId());
        return equipeDTO;
    }
    public Equipe fromEquipeDTO(EquipeDTO equipeDTO){
        Equipe equipe = new Equipe();
        BeanUtils.copyProperties(equipeDTO, equipe);
        return equipe;
    }
    public UtilisateurDTO fromUtilisateur(Utilisateur utilisateur){
        UtilisateurDTO utilisateurDTO = new UtilisateurDTO();
        BeanUtils.copyProperties(utilisateur, utilisateurDTO);
        utilisateurDTO.setEquipeId(utilisateur.getEquipe().getId());
        return utilisateurDTO;
    }
    public UtilisateurDTO fromUtilisateur1(Utilisateur utilisateur){
        UtilisateurDTO utilisateurDTO = new UtilisateurDTO();
        BeanUtils.copyProperties(utilisateur, utilisateurDTO);
        return utilisateurDTO;
    }
    public Utilisateur fromUtilisateurDTO(UtilisateurDTO utilisateurDTO){
        Utilisateur utilisateur = new Utilisateur();
        BeanUtils.copyProperties(utilisateurDTO, utilisateur);
        return utilisateur;
    }

}
