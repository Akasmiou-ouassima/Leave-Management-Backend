package com.nttdata.sid.web;

import com.nttdata.sid.dtos.CongeDTO;
import com.nttdata.sid.exceptions.*;
import com.nttdata.sid.services.CongeService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
public class CongeRestController {

    private CongeService congeService;

    public CongeRestController(CongeService congeService) {
        this.congeService = congeService;
    }
    private static final Logger LOGGER = LoggerFactory.getLogger(CongeRestController.class);
    @GetMapping("/conges")
    public ResponseEntity<List<CongeDTO>> listConge() {
        LOGGER.info("Listing all conges");
        return new ResponseEntity<>(
                congeService.listConges(),
                HttpStatus.OK
        );
    }

    @GetMapping("/conges/{id}")
    public ResponseEntity<CongeDTO> getConge(@PathVariable("id") long congeId) throws CongeNotFoundException {
        LOGGER.info("Getting conge with id {}", congeId);
        return new ResponseEntity<>(
                congeService.getConge(congeId),
                HttpStatus.OK
        );
    }

    @PostMapping("/conges")
    public ResponseEntity<CongeDTO> saveConge(@RequestBody CongeDTO congeDTO) throws UtilisateurNotFoundException, CongeSoldeInsuffisantException, CongeAlreadyExistsException {
        LOGGER.info("Saving conge {}", congeDTO);
        return new ResponseEntity<>(
                congeService.save(congeDTO),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/conges/{congeId}")
    public ResponseEntity<CongeDTO> updateConge(@PathVariable("congeId") long id, @RequestBody CongeDTO congeDTO) throws CongeNotFoundException, UtilisateurNotFoundException, CongeInvalideStateException, CongeSoldeInsuffisantException, CongeAlreadyExistsException {
        LOGGER.info("Updating conge with id {}", id);
        congeDTO.setId(id);
        return new ResponseEntity<>(
                congeService.updateConge(congeDTO),
                HttpStatus.OK
        );
    }

    @DeleteMapping("/conges/{id}")
    public ResponseEntity<Void> deleteConge(@PathVariable("id") long congeId) throws CongeNotFoundException {
        LOGGER.info("Deleting conge with id {}", congeId);
        boolean deleted = congeService.deleteConge(congeId);
        if(!deleted)
            return new  ResponseEntity<>(HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(HttpStatus.OK);
    }
    @GetMapping("conges/ByUser/{userId}/search")
    public ResponseEntity<List<CongeDTO>> searchCongeByUser(@PathVariable Long userId, @RequestParam String keyword) {
        List<CongeDTO> searchedConges = congeService.searchCongeByUser(userId, keyword);
        return ResponseEntity.ok(searchedConges);
    }

    @GetMapping("/conges/ByUser/{id}")
    private ResponseEntity<List<CongeDTO>> getCongesByUtilisateur(@PathVariable("id") long utilisateurId){
        LOGGER.info("Getting leaves By user with id {}", utilisateurId);
        return new ResponseEntity<>(
                congeService.getCongesByUtilisateur(utilisateurId),
                HttpStatus.OK
        );
    }
    @GetMapping("/conges/ByManager/{id}")
    private ResponseEntity<List<CongeDTO>> getCongesByResponsable(@PathVariable("id") long responsableId){
        LOGGER.info("Getting leaves By manager with id {}", responsableId);
        return new ResponseEntity<>(
                congeService.getCongesByResponsable(responsableId),
                HttpStatus.OK
        );
    }
    @PutMapping("/accepter/{id}")
    public ResponseEntity<CongeDTO> accepterConge(@PathVariable("id") Long id) throws CongeNotFoundException, CongeInvalideStateException, UtilisateurNotFoundException {
        CongeDTO congeDTO =  congeService.accepterConge(id);
        return new ResponseEntity<>(congeDTO, HttpStatus.OK);
    }

    @PutMapping("/refuser/{id}")
    public ResponseEntity<CongeDTO> refuserConge(@PathVariable("id") Long id) throws CongeNotFoundException, CongeInvalideStateException, UtilisateurNotFoundException {
        CongeDTO congeDTO =  congeService.refuserConge(id);
        return new ResponseEntity<>(congeDTO, HttpStatus.OK);
    }

    @GetMapping("/nbCongesPending")
    public ResponseEntity<Integer> getNbCongesPending(){
        return new ResponseEntity<>(congeService.CongePendingNb(), HttpStatus.OK);
    }
    @GetMapping("/nbCongesApproved")
    public ResponseEntity<Integer> getNbCongesApproved(){
        return new ResponseEntity<>(congeService.CongeApprovedNb(), HttpStatus.OK);
    }
    @GetMapping("/nbCongesRefused")
    public ResponseEntity<Integer> getNbCongesRefused(){
        return new ResponseEntity<>(congeService.CongeRefusedNb(), HttpStatus.OK);
    }
    @GetMapping("/CongesAccepted")
    public ResponseEntity<List<CongeDTO>> getCongesAccepted(){
        return new ResponseEntity<>(congeService.getCongesAccepted(), HttpStatus.OK);
    }
    @PutMapping("/conges/{id}/uploadPdf")
    public ResponseEntity<CongeDTO> uploadCongePdf(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        try {
            CongeDTO uploadedConge = congeService.uploadCongePdf(id, file);
            return ResponseEntity.ok(uploadedConge);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (CongeNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
    @GetMapping("/nbCongesByUser/{id}/By/{etat}")
    public ResponseEntity<Integer> getNbCongesByUserByEtat(@PathVariable("id") int id,@PathVariable("etat") String etat){
        return new ResponseEntity<>(congeService.getCongesByUtilisateurByEtat(id,etat), HttpStatus.OK);
    }
    @GetMapping("/nbCongesBySalarie/{id}")
    public ResponseEntity<Integer> getNbCongesByUser(@PathVariable("id") int id){
        return new ResponseEntity<>(congeService.getNbCongesByUser(id), HttpStatus.OK);
    }

    @GetMapping("/nbConges/By/{annee}")
    public ResponseEntity<List<Integer>> getNbCongesByMoisAnnee(@PathVariable("annee") int year){
        System.out.println("controller getNbCongesByMoisAnnee"+year);
        return new ResponseEntity<>(congeService.getCongesByMonthAndYear(year), HttpStatus.OK);
    }
    @GetMapping("/nbCongesByMoisByUser/By/{id}")
    public ResponseEntity<List<Integer>> getNbCongesByMoisUser(@PathVariable("id") int id){
        System.out.println("controller getNbCongesByMoisUser"+id);
        return new ResponseEntity<>(congeService.getCongesByMoisByUser(id), HttpStatus.OK);
    }
}

