package com.nttdata.sid.services;


import com.nttdata.sid.dtos.EquipeDTO;
import com.nttdata.sid.entities.Equipe;
import com.nttdata.sid.entities.Utilisateur;
import com.nttdata.sid.enums.UserType;
import com.nttdata.sid.exceptions.EquipeAlreadyExistsException;
import com.nttdata.sid.exceptions.EquipeNotFoundException;
import com.nttdata.sid.exceptions.UtilisateurNotFoundException;
import com.nttdata.sid.mappers.MappersImpl;
import com.nttdata.sid.repositories.EquipeRepository;
import com.nttdata.sid.repositories.UtilisateurRepository;
import com.nttdata.sid.security.entities.AppRole;
import com.nttdata.sid.security.entities.AppUser;
import com.nttdata.sid.security.repositories.AppUserRepository;
import com.nttdata.sid.security.services.ISecurityService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class EquipeServiceImpl implements EquipeService {

    private EquipeRepository equipeRepository;
    private UtilisateurRepository utilisateurRepository;
    private ISecurityService iSecurityService;
    private AppUserRepository appUserRepository;
    private MappersImpl dtoMapper;

    public EquipeServiceImpl(EquipeRepository equipeRepository, UtilisateurRepository utilisateurRepository, ISecurityService iSecurityService, AppUserRepository appUserRepository, MappersImpl dtoMapper) {
        this.equipeRepository = equipeRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.iSecurityService = iSecurityService;
        this.appUserRepository = appUserRepository;
        this.dtoMapper = dtoMapper;
    }
    private static final Logger logger = LoggerFactory.getLogger(EquipeServiceImpl.class);
    @Override
    public EquipeDTO save(EquipeDTO equipeDTO) throws UtilisateurNotFoundException, EquipeAlreadyExistsException {
        Equipe equipe = dtoMapper.fromEquipeDTO(equipeDTO);
        Utilisateur utilisateur=utilisateurRepository.findById(equipeDTO.getResponsableId()).orElseThrow(()-> new UtilisateurNotFoundException("User not found with this id: " + equipeDTO.getResponsableId()));
        equipe.setUtilisateur(utilisateur);
        Optional<Equipe> existingEquipe = equipeRepository.findEquipeByNom(equipe.getNom());
        if (existingEquipe.isPresent()) {
            logger.info("Team already exists with id {}", equipeDTO.getId());
            throw new EquipeAlreadyExistsException("Team already exists with ID = "+equipeDTO.getId());
        }
        logger.info("Saving new equipe: {}", equipeDTO);
        logger.info("type respo "+equipe.getUtilisateur().getType());
        equipe.getUtilisateur().setType(UserType.RESPONSABLE);
        AppUser appUser=iSecurityService.getAppUserById(equipe.getUtilisateur().getId());
        Collection<AppRole> roles =new ArrayList<>();
        AppRole appRole = iSecurityService.findRoleByRoleName("RESPONSABLE");
        roles.add(appRole);
        appUser.setUserRoles(roles);
        appUserRepository.save(appUser);
        Equipe savedEquipe = equipeRepository.save(equipe);
        logger.info("id equipe "+savedEquipe.getId());
        utilisateurRepository.save(savedEquipe.getUtilisateur());
        logger.info("type respo after save "+equipe.getUtilisateur().getType());
        EquipeDTO savedEquipeDTO = dtoMapper.fromEquipe(savedEquipe);
        logger.info("Team saved: {}", savedEquipeDTO);
        return savedEquipeDTO;
    }

    @Override
    public EquipeDTO getEquipe(Long equipeId) {
        logger.info("Retrieving team with ID: {}", equipeId);
        Equipe equipe = equipeRepository.findById(equipeId).orElseThrow(() -> new RuntimeException("Team not found with this id: " + equipeId));
        EquipeDTO equipeDTO = dtoMapper.fromEquipe(equipe);
        logger.info("Retrieved team: {}", equipeDTO);
        return equipeDTO;
    }

    @Override
    public EquipeDTO updateEquipe(EquipeDTO equipeDTO) throws EquipeNotFoundException, UtilisateurNotFoundException, EquipeAlreadyExistsException {
        logger.info("Updating team: {}", equipeDTO);
        AppRole appRole = iSecurityService.findRoleByRoleName("RESPONSABLE");
        Equipe preUpdate = equipeRepository.findById(equipeDTO.getId()).orElseThrow(()-> new EquipeNotFoundException("Equipe not found with this id: " + equipeDTO.getId()));
        Utilisateur managPrevious = preUpdate.getUtilisateur();
        Equipe equipe = dtoMapper.fromEquipeDTO(equipeDTO);
        Optional<Equipe> existingEquipe = equipeRepository.findEquipeByNom(equipe.getNom());
        if (existingEquipe.isPresent() && !existingEquipe.get().getId().equals(equipeDTO.getId())) {
            logger.info("Team already exists with id {}", equipeDTO.getId());
            throw new EquipeAlreadyExistsException("Team already exists with ID = "+equipeDTO.getId());
        }
        Utilisateur utilisateur=utilisateurRepository.findById(equipeDTO.getResponsableId()).orElseThrow(()-> new UtilisateurNotFoundException("User not found with this id: " + equipeDTO.getResponsableId()));
        AppUser userPrevious = appUserRepository.findAppUserByEmail(managPrevious.getEmail());
        AppUser userNew = appUserRepository.findAppUserByEmail(utilisateur.getEmail());
        if(utilisateur.getType()==UserType.SALARIE){
            logger.info("user new is a salarie");
            userNew.getUserRoles().add(appRole);
            iSecurityService.updateRoleUser(userNew);
            logger.info("user new became respo");
        }
        equipe.setUtilisateur(utilisateur);
        equipe.getUtilisateur().setType(UserType.RESPONSABLE);
        utilisateurRepository.save(equipe.getUtilisateur());
        Equipe updatedEquipe = equipeRepository.save(equipe);

        if(!(managPrevious.getId() == equipe.getUtilisateur().getId())){
            List<Equipe> equipes =  equipeRepository.findEquipesByUtilisateurId(managPrevious.getId());
            if(equipes.size()==0){
                managPrevious.setType(UserType.SALARIE);
                utilisateurRepository.save(managPrevious);
                userPrevious.getUserRoles().remove(appRole);
                iSecurityService.updateRoleUser(userPrevious);
                System.out.println("manager previous is not more a manager  ");
            }
        }
        EquipeDTO updatedEquipeDTO = dtoMapper.fromEquipe(updatedEquipe);
        logger.info("Team updated: {}", updatedEquipeDTO);
        return updatedEquipeDTO;
    }

    @Override
    public boolean deleteEquipe(Long equipeId) {
        Equipe equipe=equipeRepository.findById(equipeId).orElseThrow(() -> new RuntimeException("Team not found with this id: " + equipeId));
        if (equipe.getUtilisateurs().isEmpty() ) {
            logger.info("Deleting team with ID: {}", equipeId);
            equipeRepository.deleteById(equipeId);
            logger.info("Team deleted with ID: {}", equipeId);
            return true;
        }
        return false;
    }

    @Override
    public List<EquipeDTO> listEquipes() {
        logger.info("Listing all teams");
        List<Equipe> equipes = equipeRepository.findAll();
        List<EquipeDTO> equipeDTOS = equipes.stream().map(equipe -> dtoMapper.fromEquipe(equipe)).collect(Collectors.toList());
        logger.info("Retrieved {} teams", equipeDTOS.size());
        return equipeDTOS;
    }

    @Override
    public List<EquipeDTO> getEquipesByUtilisateur(Long utilisateurId) throws UtilisateurNotFoundException {
        logger.info("Retrieving team by user with ID: {}", utilisateurId);
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId).orElseThrow(()-> new UtilisateurNotFoundException("User not found with this id: " + utilisateurId));
        Equipe equipe = utilisateur.getEquipe();
        EquipeDTO equipeDTO = dtoMapper.fromEquipe(equipe);
        List<EquipeDTO> equipeDTOS=new ArrayList<>();
        equipeDTOS.add(equipeDTO);
        if (utilisateur.getType().equals(UserType.RESPONSABLE)) {
            List<Equipe> equipes = equipeRepository.findEquipesByUtilisateurId(utilisateurId);
            List<EquipeDTO> equipeDTOS1 = equipes.stream().map(equipe1 -> dtoMapper.fromEquipe(equipe1)).collect(Collectors.toList());
            for (EquipeDTO equipe1 : equipeDTOS1){
                equipeDTOS.add(equipe1);
            }
        }
        logger.info("Retrieved team: {}", equipeDTO);
        return equipeDTOS;
    }

    @Override
    public EquipeDTO uploadTeamPhoto(Long id, MultipartFile file) throws IOException, EquipeNotFoundException {
        Equipe equipe = equipeRepository.findById(id).orElseThrow(() -> new EquipeNotFoundException("Team not found with this id: " + id));

        if (!isValidImageFileType(file.getContentType())) {
            throw new IllegalArgumentException("Unsupported file type. Only JPEG, and PNG images are allowed.");
        }
        // Generate a unique file name to avoid conflicts
        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();

        String DIRECTORY = "C:\\Users\\ouasi\\Downloads\\Leave-Management-Frontend\\src\\assets\\img\\teams";
        Path fileStorage = Paths.get(DIRECTORY, filename).toAbsolutePath().normalize();

        try {
            // Create the directory if it doesn't exist
            Files.createDirectories(fileStorage.getParent());

            // Copy the file to the target path
            Files.copy(file.getInputStream(), fileStorage, StandardCopyOption.REPLACE_EXISTING);

            // Update the equipe with the image file path
            equipe.setImage(filename);
            equipeRepository.save(equipe);
        } catch (IOException e) {
            throw new IOException("Failed to save the file. Please try again later.", e);
        }
        return dtoMapper.fromEquipe(equipe);
    }


    private boolean isValidImageFileType(String contentType) {
        return contentType != null && (contentType.equals(MediaType.IMAGE_JPEG_VALUE) ||
                        contentType.equals(MediaType.IMAGE_PNG_VALUE)
        );
    }

    @Override
    public int EquipesNb() {
        List<Equipe> equipes = equipeRepository.findAll();
        return equipes.size();
    }
}

