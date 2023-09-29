package com.nttdata.sid.web;

import com.nttdata.sid.dtos.EquipeDTO;
import com.nttdata.sid.dtos.UtilisateurDTO;
import com.nttdata.sid.exceptions.EquipeAlreadyExistsException;
import com.nttdata.sid.exceptions.EquipeNotFoundException;
import com.nttdata.sid.exceptions.UtilisateurNotFoundException;
import com.nttdata.sid.services.EquipeService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class EquipeRestController {

    private EquipeService equipeService;

    public EquipeRestController(EquipeService equipeService) {
        this.equipeService = equipeService;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(EquipeRestController.class);

    @GetMapping("/equipes")
    public ResponseEntity<List<EquipeDTO>> listEquipe() {
        LOGGER.info("Listing all equipes");
        return new ResponseEntity<>(
                equipeService.listEquipes(),
                HttpStatus.OK
        );
    }

    @GetMapping("/equipes/{id}")
    public ResponseEntity<EquipeDTO> getEquipe(@PathVariable("id") long equipeId) {
        LOGGER.info("Getting equipe with ID {}", equipeId);
        return new ResponseEntity<>(
                equipeService.getEquipe(equipeId),
                HttpStatus.OK
        );
    }
    @PostAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/equipes")
    public ResponseEntity<EquipeDTO> saveEquipe(@RequestBody EquipeDTO equipeDTO) throws UtilisateurNotFoundException, EquipeAlreadyExistsException {
        LOGGER.info("Saving equipe {}", equipeDTO);
        return new ResponseEntity<>(
                equipeService.save(equipeDTO),
                HttpStatus.CREATED
        );
    }
    @PostAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/equipes/{id}")
    public ResponseEntity<EquipeDTO> updateEquipe(@PathVariable("id") long id, @RequestBody EquipeDTO equipeDTO) throws EquipeNotFoundException, UtilisateurNotFoundException, EquipeAlreadyExistsException {
        LOGGER.info("Updating equipe with ID {}", id);
        equipeDTO.setId(id);
        return new ResponseEntity<>(
                equipeService.updateEquipe(equipeDTO),
                HttpStatus.OK
        );
    }

    @GetMapping("equipes/ByUser/{id}")
    public ResponseEntity<List<EquipeDTO>> getEquipesByUtilisateur(@PathVariable("id") long utilisateurId) throws UtilisateurNotFoundException {
        LOGGER.info("Getting teams by user with ID {} ",utilisateurId);
        return new ResponseEntity<>(
                equipeService.getEquipesByUtilisateur(utilisateurId),
                HttpStatus.OK
        );
    }

    @PostAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/equipes/{id}")
    public ResponseEntity<Boolean> deleteEquipe(@PathVariable("id") long equipeId) {
        LOGGER.info("Deleting equipe with ID {}", equipeId);
        return new ResponseEntity<>(equipeService.deleteEquipe(equipeId),HttpStatus.OK);
    }

    @PutMapping("/equipes/{id}/uploadPhoto")
    public ResponseEntity<EquipeDTO> uploadTeamPhoto(@PathVariable Long id, @RequestParam("file") MultipartFile file){
        try {
            LOGGER.info("upload image "+file);
            EquipeDTO updatedEquipeDTO = equipeService.uploadTeamPhoto(id, file);
            return ResponseEntity.ok(updatedEquipeDTO);
        } catch (IOException | EquipeNotFoundException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    @GetMapping("/nbequipes")
    public ResponseEntity<Integer> EquipesNb() {
        LOGGER.info("Getting number of teams");
        return new ResponseEntity<>(
                equipeService.EquipesNb(),
                HttpStatus.OK
        );
    }
}
