package com.nttdata.sid.services;


import com.nttdata.sid.dtos.UtilisateurDTO;
import com.nttdata.sid.entities.Equipe;
import com.nttdata.sid.entities.PasswordResetToken;
import com.nttdata.sid.entities.Utilisateur;
import com.nttdata.sid.enums.Status;
import com.nttdata.sid.enums.UserType;
import com.nttdata.sid.exceptions.EquipeNotFoundException;
import com.nttdata.sid.exceptions.UserAlreadyExistxException;
import com.nttdata.sid.exceptions.UtilisateurNotFoundException;
import com.nttdata.sid.mappers.MappersImpl;
import com.nttdata.sid.repositories.EquipeRepository;
import com.nttdata.sid.repositories.PasswordResetTokenRepository;
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
import org.springframework.security.crypto.password.PasswordEncoder;
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
public class UtilisateurServiceImpl implements UtilisateurService {
    private UtilisateurRepository utilisateurRepository;
    private EquipeRepository equipeRepository;
    private MappersImpl dtoMapper;
    private PasswordEncoder passwordEncoder;
    private ISecurityService iSecurityService;
    private PasswordResetTokenRepository passwordResetTokenRepository;
    private EmailService emailService;
    private AppUserRepository appUserRepository;

    public UtilisateurServiceImpl(UtilisateurRepository utilisateurRepository, EquipeRepository equipeRepository,
                                  MappersImpl dtoMapper, PasswordEncoder passwordEncoder,
                                  ISecurityService iSecurityService, PasswordResetTokenRepository passwordResetTokenRepository,
                                  EmailService emailService,
                                  AppUserRepository appUserRepository) {
        this.utilisateurRepository = utilisateurRepository;
        this.equipeRepository = equipeRepository;
        this.dtoMapper = dtoMapper;
        this.passwordEncoder = passwordEncoder;
        this.iSecurityService = iSecurityService;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.emailService = emailService;
        this.appUserRepository = appUserRepository;
    }

    private static final Logger logger = LoggerFactory.getLogger(UtilisateurServiceImpl.class);

    @Override
    public UtilisateurDTO save(UtilisateurDTO utilisateurDTO) throws EquipeNotFoundException, UserAlreadyExistxException {
        Utilisateur utilisateur = dtoMapper.fromUtilisateurDTO(utilisateurDTO);
        Equipe equipe = equipeRepository.findById(utilisateurDTO.getEquipeId())
                .orElseThrow(() -> new EquipeNotFoundException("Equipe not found with ID: " + utilisateurDTO.getEquipeId()));
        utilisateur.setEquipe(equipe);

        Optional<Utilisateur> existingUtilisateur = utilisateurRepository.findUtilisateurByEmail(utilisateur.getEmail());
        if (existingUtilisateur.isPresent()) {
            logger.info("Utilisateur already exists with id {}", utilisateurDTO.getId());
            throw new UserAlreadyExistxException("Utilisateur already exists with id {}" + utilisateurDTO.getId());
        }
        utilisateur.setStatus(Status.ACTIVE);
        utilisateur.setType(UserType.SALARIE);
        utilisateur.setSolde(18);
        Utilisateur savedUtilisateur = utilisateurRepository.save(utilisateur);

        AppUser appUser = new AppUser();
        String password = generateUniquePassword();
        try {
            emailService.sendEmailNouveauUser(utilisateur.getId(),password);
        } catch (UtilisateurNotFoundException e) {
            throw new RuntimeException(e);
        }
        appUser.setPassword(password);
        appUser.setId(savedUtilisateur.getId());
        appUser.setEmail(savedUtilisateur.getEmail());
        Collection<AppRole> roles = new ArrayList<>();
        AppRole appRole = iSecurityService.findRoleByRoleName("SALARIE");
        roles.add(appRole);
        appUser.setUserRoles(roles);
        iSecurityService.addNewUser(appUser);

        UtilisateurDTO savedUtilisateurDTO = dtoMapper.fromUtilisateur(savedUtilisateur);
        return savedUtilisateurDTO;
    }

    @Override
    public UtilisateurDTO getUtilisateur(Long utilisateurId) {
        logger.info("Retrieving utilisateur with ID: {}", utilisateurId);
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId).orElse(null);
        if (utilisateur.getId()==1) {
            UtilisateurDTO utilisateurDTO = dtoMapper.fromUtilisateur1(utilisateur);
            return utilisateurDTO;
        }
        UtilisateurDTO utilisateurDTO = dtoMapper.fromUtilisateur(utilisateur);
        logger.info("Retrieved utilisateur: {}", utilisateurDTO);
        return utilisateurDTO;
    }

    @Override
    public UtilisateurDTO updateUtilisateur(UtilisateurDTO utilisateurDTO) throws EquipeNotFoundException, UserAlreadyExistxException {
        logger.info("Updating utilisateur: {}", utilisateurDTO);
        Optional<Utilisateur> existingUtilisateur = utilisateurRepository.findUtilisateurByEmail(utilisateurDTO.getEmail());
        if (existingUtilisateur.isPresent() && existingUtilisateur.get().getId()!=utilisateurDTO.getId()) {
            logger.info("Utilisateur already exists with id {}", utilisateurDTO.getId());
            throw new UserAlreadyExistxException("email already exists => update refused!!!");
        }
        Optional<Utilisateur> previousUser = utilisateurRepository.findById(utilisateurDTO.getId());
        int solde = utilisateurDTO.getSolde();
        UserType type = utilisateurDTO.getType();
        Utilisateur utilisateur = dtoMapper.fromUtilisateurDTO(utilisateurDTO);
        if(utilisateur.getImage().isEmpty()){
            utilisateur.setImage(previousUser.get().getImage());
        }
        Equipe equipe = equipeRepository.findById(utilisateurDTO.getEquipeId())
                .orElseThrow(() -> new EquipeNotFoundException("Equipe not found with ID: " + utilisateurDTO.getEquipeId()));
        utilisateur.setEquipe(equipe);
        utilisateur.setSolde(solde);
        utilisateur.setType(type);

        AppUser appUser = appUserRepository.findAppUserByEmail(previousUser.get().getEmail());
        appUser.setEmail(utilisateur.getEmail());
        iSecurityService.updateRoleUser(appUser);

        Utilisateur updatedUtilisateur = utilisateurRepository.save(utilisateur);
        UtilisateurDTO updatedUtilisateurDTO = dtoMapper.fromUtilisateur(updatedUtilisateur);
        logger.info("Utilisateur updated: {}", updatedUtilisateurDTO);
        return updatedUtilisateurDTO;
    }

    @Override
    public boolean deleteUtilisateur(Long utilisateurId) {
        Utilisateur u = utilisateurRepository.findById(utilisateurId).get();
        if (u == null) {
            log.error("Utilisateur doesn't exist");
            return false;
        } else if ((u.getType().equals(UserType.RESPONSABLE))) {
            return false;
        } else {
            logger.info("Deleting utilisateur with ID: {}", utilisateurId);
            utilisateurRepository.deleteById(utilisateurId);
            AppUser appUser = appUserRepository.findAppUserByEmail(u.getEmail());
            iSecurityService.deleteUser(appUser);
            logger.info("Utilisateur deleted with ID: {}", utilisateurId);
            return true;
        }
    }

    @Override
    public List<UtilisateurDTO> listUtilisateurs() {
        logger.info("Listing all utilisateurs");
        List<Utilisateur> utilisateurs = utilisateurRepository.findAll();
        utilisateurs.removeIf(utilisateur -> utilisateur.getId() == 1);
        List<UtilisateurDTO> utilisateurDTOS = utilisateurs.stream().map(utilisateur -> dtoMapper.fromUtilisateur(utilisateur)).collect(Collectors.toList());
        logger.info("Retrieved {} utilisateurs", utilisateurDTOS.size());
        return utilisateurDTOS;
    }

    @Override
    public List<Utilisateur> searchUtilisateur(String keyword) {
        logger.info("Searching for utilisateur with keyword: {}", keyword);
        List<Utilisateur> utilisateurs = utilisateurRepository.findUtilisateurByNomIsContainingIgnoreCase(keyword);
        logger.info("Retrieved {} utilisateurs", utilisateurs.size());
        return utilisateurs;
    }

    @Override
    public List<UtilisateurDTO> getUtilisateursByEquipe(Long equipeId) throws EquipeNotFoundException {
        Equipe equipe = equipeRepository.findById(equipeId).orElseThrow(() -> new EquipeNotFoundException("Team not found with this id: " + equipeId));
        List<Utilisateur> utilisateurs = utilisateurRepository.findUtilisateursByEquipeId(equipeId);
        List<UtilisateurDTO> utilisateurDTOS = utilisateurs.stream().map(utilisateur -> dtoMapper.fromUtilisateur(utilisateur)).collect(Collectors.toList());
        return utilisateurDTOS;
    }

    @Override
    public int ResponsablesNb() {
        List<Utilisateur> utilisateurs = utilisateurRepository.findUtilisateursByType(UserType.RESPONSABLE);
        utilisateurs = utilisateurs.stream().filter(utilisateur -> utilisateur.getId() != 1).collect(Collectors.toList());
        return utilisateurs.size();
    }

    @Override
    public int SalariesNb() {
        List<Utilisateur> utilisateurs = utilisateurRepository.findUtilisateursByType(UserType.SALARIE);
        return utilisateurs.size();
    }

    @Override
    public UtilisateurDTO uploadUserPhoto(Long id, MultipartFile file) throws IOException, UtilisateurNotFoundException {
        Utilisateur utilisateur = utilisateurRepository.findById(id).orElseThrow(() -> new UtilisateurNotFoundException("User not found with this id: " + id));

        if (!isValidImageFileType(file.getContentType())) {
            throw new IllegalArgumentException("Unsupported file type. Only JPEG, JPG and PNG images are allowed.");
        }

        // Generate a unique file name to avoid conflicts
        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();

        String DIRECTORY = "C:\\Users\\ouasi\\Downloads\\Leave-Management-Frontend\\src\\assets\\img\\users";
        Path fileStorage = Paths.get(DIRECTORY, filename).toAbsolutePath().normalize();

        try {
            // Create the directory if it doesn't exist
            Files.createDirectories(fileStorage.getParent());

            // Copy the file to the target path
            Files.copy(file.getInputStream(), fileStorage, StandardCopyOption.REPLACE_EXISTING);

            // Update the equipe with the image file path
            utilisateur.setImage(filename);
            utilisateurRepository.save(utilisateur);
        } catch (IOException e) {
            throw new IOException("Failed to save the file. Please try again later.", e);
        }
        if(utilisateur.getEquipe()!= null){
            return dtoMapper.fromUtilisateur(utilisateur);
        }else {
            return dtoMapper.fromUtilisateur1(utilisateur);
        }
    }


    private boolean isValidImageFileType(String contentType) {
        return contentType != null && (contentType.equals(MediaType.IMAGE_JPEG_VALUE) ||
                contentType.equals(MediaType.IMAGE_PNG_VALUE)
        );
    }

    @Override
    public void initiatePasswordReset(String email) {
        try {
            AppUser user = appUserRepository.findAppUserByEmail(email);
            if(user==null){
                throw new UtilisateurNotFoundException("User not found with this email");
            }
            String token = generatePasswordResetToken();

            PasswordResetToken resetToken = new PasswordResetToken();
            System.out.println("le token " + token);
            resetToken.setToken(token);
            resetToken.setExpiryDate(calculateExpiryDate());

            resetToken.setUser(user);
            passwordResetTokenRepository.save(resetToken);
            emailService.sendPasswordResetEmail(user.getEmail(), token);
        } catch (Exception e) {
            System.out.println("erreur in initiatePasswordReset");
        }
    }

    @Override
    public Boolean completePasswordReset(String token, String newPassword) {
        System.out.println("token " + token + " pass " + newPassword);
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token);
        if (resetToken != null && !resetToken.isExpired()) {
            AppUser user = resetToken.getUser();
            user.setPassword(passwordEncoder.encode(newPassword));
            appUserRepository.save(user);
            System.out.println("saved new pass");
            passwordResetTokenRepository.delete(resetToken);
            return true;
        } else {
            System.out.println("Invalid or expired token");
            return false;
        }
    }

    private String generatePasswordResetToken() {
        return UUID.randomUUID().toString();
    }

    private Date calculateExpiryDate() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.MINUTE, 30);
        return cal.getTime();
    }

    @Override
    public UtilisateurDTO editProfil(UtilisateurDTO utilisateurDTO, String newPassword) throws EquipeNotFoundException {
        int solde = utilisateurDTO.getSolde();
        UserType type = utilisateurDTO.getType();
        Utilisateur utilisateur = dtoMapper.fromUtilisateurDTO(utilisateurDTO);

        if (utilisateurDTO.getEquipeId() != null) {
            Equipe equipe = equipeRepository.findById(utilisateurDTO.getEquipeId())
                    .orElseThrow(() -> new EquipeNotFoundException("Equipe not found with ID: " + utilisateurDTO.getEquipeId()));
            utilisateur.setEquipe(equipe);
            utilisateur.setSolde(solde);
            utilisateur.setType(type);
            Utilisateur editedUser = utilisateurRepository.save(utilisateur);
            UtilisateurDTO editedUserDTO = dtoMapper.fromUtilisateur(editedUser);
            AppUser appUser = appUserRepository.findAppUserByEmail(utilisateur.getEmail());
            if(newPassword != null){
                appUser.setPassword(passwordEncoder.encode(newPassword));
                appUserRepository.save(appUser);
            }
            return editedUserDTO;
        }else {
            utilisateur.setSolde(solde);
            utilisateur.setType(type);
            Utilisateur editedUser = utilisateurRepository.save(utilisateur);
            UtilisateurDTO editedUserDTO = dtoMapper.fromUtilisateur1(editedUser);
            AppUser appUser = appUserRepository.findAppUserByEmail(utilisateur.getEmail());
            if(newPassword != null){
                appUser.setPassword(passwordEncoder.encode(newPassword));
                appUserRepository.save(appUser);
            }
            return editedUserDTO;
        }



    }

    public  String generateUniquePassword() {

        long currentTimeMillis = System.currentTimeMillis();
        String password = String.valueOf(currentTimeMillis);
        return password;
    }
}

