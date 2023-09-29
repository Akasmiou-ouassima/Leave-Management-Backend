package com.nttdata.sid.web;

import com.nttdata.sid.dtos.EditProfileRequest;
import com.nttdata.sid.dtos.UtilisateurDTO;
import com.nttdata.sid.entities.Utilisateur;
import com.nttdata.sid.exceptions.EquipeNotFoundException;
import com.nttdata.sid.exceptions.UserAlreadyExistxException;
import com.nttdata.sid.exceptions.UtilisateurNotFoundException;
import com.nttdata.sid.security.entities.AppUser;
import com.nttdata.sid.services.UtilisateurService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@Slf4j
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class UtilisateurRestController {

    private UtilisateurService utilisateurService;

    public UtilisateurRestController(UtilisateurService utilisateurService) {
        this.utilisateurService = utilisateurService;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(UtilisateurRestController.class);

    @PostAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/utilisateurs")
    public ResponseEntity<List<UtilisateurDTO>> listUtilisateurs() {
        LOGGER.info("Listing all utilisateurs");
        return new ResponseEntity<>(
                utilisateurService.listUtilisateurs(),
                HttpStatus.OK
        );
    }

    @GetMapping("/utilisateurs/{id}")
    public ResponseEntity<UtilisateurDTO> getUtilisateur(@PathVariable("id") long utilisateurId) {
        LOGGER.info("Getting utilisateur with id {}", utilisateurId);
        return new ResponseEntity<>(
                utilisateurService.getUtilisateur(utilisateurId),
                HttpStatus.OK
        );
    }

    @GetMapping("/membersEquipe/{id}")
    public ResponseEntity<List<UtilisateurDTO>> getMembersEquipe(@PathVariable("id") long equipeId) throws EquipeNotFoundException {
        LOGGER.info("Getting utilisateur in equipe with id {} : ", equipeId);
        return new ResponseEntity<>(
                utilisateurService.getUtilisateursByEquipe(equipeId),
                HttpStatus.OK
        );
    }
    @PostAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/utilisateurs")
    public ResponseEntity<UtilisateurDTO> saveUtilisateur(@RequestBody UtilisateurDTO utilisateurDTO) throws EquipeNotFoundException, UserAlreadyExistxException {
        LOGGER.info("Saving utilisateur {}", utilisateurDTO);
        return new ResponseEntity<>(
                utilisateurService.save(utilisateurDTO),
                HttpStatus.CREATED
        );
    }
    @PostAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/utilisateurs/{utilisateurId}")
    public ResponseEntity<UtilisateurDTO> updateUtilisateur(@PathVariable("utilisateurId") long id, @RequestBody UtilisateurDTO utilisateurDTO) throws EquipeNotFoundException, UserAlreadyExistxException {
        LOGGER.info("Updating utilisateur with id {}", id);
        utilisateurDTO.setId(id);
        return new ResponseEntity<>(
                utilisateurService.updateUtilisateur(utilisateurDTO),
                HttpStatus.OK
        );
    }
    @PostAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/utilisateurs/{id}")
    public ResponseEntity<Void> deleteUtilisateur(@PathVariable("id") long utilisateurId) {
        LOGGER.info("Deleting utilisateur with id {}", utilisateurId);
        boolean deleted = utilisateurService.deleteUtilisateur(utilisateurId);
        if (!deleted)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/utilisateurs/search")
    public ResponseEntity<List<Utilisateur>> searchUtilisateurs(@RequestParam(name = "keyword", defaultValue = "") String keyword) {
        LOGGER.info("Searching utilisateurs for keyword {}", keyword);
        return new ResponseEntity<>(
                utilisateurService.searchUtilisateur("%" + keyword + "%"),
                HttpStatus.OK
        );
    }
    @GetMapping("/nbResponsables")
    public ResponseEntity<Integer> ResponsablesNb() {
        LOGGER.info("Getting number of Managers");
        return new ResponseEntity<>(
                utilisateurService.ResponsablesNb(),
                HttpStatus.OK
        );
    }
    @GetMapping("/nbSalaries")
    public ResponseEntity<Integer> SalariesNb() {
        LOGGER.info("Getting number of Employees");
        return new ResponseEntity<>(
                utilisateurService.SalariesNb(),
                HttpStatus.OK
        );
    }
    @PutMapping("/utilisateurs/{id}/uploadPhoto")
    public ResponseEntity<UtilisateurDTO> uploadUserPhoto(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        try {
            LOGGER.info("Uploading user photo for user with id {}", id);
            UtilisateurDTO updatedUserDTO = utilisateurService.uploadUserPhoto(id, file);
            return ResponseEntity.ok(updatedUserDTO);
        } catch (IOException | UtilisateurNotFoundException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/forgot-password/{email}")
    public ResponseEntity<String> forgotPassword(@PathVariable("email") String email) {
        utilisateurService.initiatePasswordReset(email);
        return ResponseEntity.ok("Password reset link sent successfully");
    }

    @GetMapping("/reset-password/{token}/{newPassword}")
    public ResponseEntity<Boolean> resetPassword(@PathVariable("token") String token, @PathVariable("newPassword") String newPassword ) {
        System.out.println("/reset-password/{token}/{newPassword} ");
        return new ResponseEntity<>(utilisateurService.completePasswordReset(token,newPassword),HttpStatus.OK);
    }
    @PutMapping("/editProfil/{id}")
    public ResponseEntity<UtilisateurDTO> editProfil(@PathVariable("id") long id, @RequestBody EditProfileRequest request) {
        try {
            UtilisateurDTO utilisateurDTO = request.getUser();
            String newPassword = request.getNewPassword();
            utilisateurDTO.setId(id);
            UtilisateurDTO editedUserDTO = utilisateurService.editProfil(utilisateurDTO, newPassword);
            return new ResponseEntity<>(editedUserDTO, HttpStatus.OK);
        } catch (EquipeNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
