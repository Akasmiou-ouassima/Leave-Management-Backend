package com.nttdata.sid.services;
import com.nttdata.sid.dtos.EquipeDTO;
import com.nttdata.sid.exceptions.EquipeAlreadyExistsException;
import com.nttdata.sid.exceptions.EquipeNotFoundException;
import com.nttdata.sid.exceptions.UtilisateurNotFoundException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface EquipeService {
    EquipeDTO save(EquipeDTO equipeDTO) throws UtilisateurNotFoundException, EquipeAlreadyExistsException;
    EquipeDTO getEquipe(Long equipeId);
    EquipeDTO updateEquipe(EquipeDTO equipeDTO) throws EquipeNotFoundException, UtilisateurNotFoundException, EquipeAlreadyExistsException;
    boolean deleteEquipe(Long equipeId);
    List<EquipeDTO> listEquipes();

    List<EquipeDTO> getEquipesByUtilisateur(Long utilisateurId) throws UtilisateurNotFoundException;

    EquipeDTO uploadTeamPhoto(Long id, MultipartFile file) throws IOException, EquipeNotFoundException;
    int EquipesNb();
}
