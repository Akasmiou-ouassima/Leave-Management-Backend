package com.nttdata.sid.services;

import com.nttdata.sid.dtos.UtilisateurDTO;
import com.nttdata.sid.entities.Utilisateur;
import com.nttdata.sid.exceptions.EquipeNotFoundException;
import com.nttdata.sid.exceptions.UserAlreadyExistxException;
import com.nttdata.sid.exceptions.UtilisateurNotFoundException;
import com.nttdata.sid.security.entities.AppUser;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;


public interface UtilisateurService {
    UtilisateurDTO save(UtilisateurDTO utilisateurDTO) throws EquipeNotFoundException, UserAlreadyExistxException;
    UtilisateurDTO getUtilisateur(Long utilisateurId);
    UtilisateurDTO updateUtilisateur(UtilisateurDTO utilisateurDTO) throws EquipeNotFoundException, UserAlreadyExistxException;
    boolean deleteUtilisateur(Long utilisateurId);
    List<UtilisateurDTO> listUtilisateurs();
    List<Utilisateur> searchUtilisateur(String keyword);

    List<UtilisateurDTO> getUtilisateursByEquipe(Long equipeId) throws EquipeNotFoundException;
     int ResponsablesNb();
     int SalariesNb();
    UtilisateurDTO uploadUserPhoto(Long id, MultipartFile file) throws IOException, UtilisateurNotFoundException;
    void initiatePasswordReset(String email);

    Boolean completePasswordReset(String token, String newPassword);

    UtilisateurDTO editProfil(UtilisateurDTO utilisateurDTO, String newPassword) throws EquipeNotFoundException;
}
