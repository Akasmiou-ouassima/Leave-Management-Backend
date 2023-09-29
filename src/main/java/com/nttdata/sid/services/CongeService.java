package com.nttdata.sid.services;

import com.nttdata.sid.dtos.CongeDTO;
import com.nttdata.sid.dtos.UtilisateurDTO;
import com.nttdata.sid.enums.Type;
import com.nttdata.sid.exceptions.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.List;

public interface CongeService {
    CongeDTO save(CongeDTO congeDTO) throws UtilisateurNotFoundException, CongeAlreadyExistsException, CongeSoldeInsuffisantException;
    List<CongeDTO> listConges();
    CongeDTO getConge(Long congeId) throws CongeNotFoundException;
    CongeDTO updateConge(CongeDTO congeDTO) throws CongeNotFoundException, UtilisateurNotFoundException, CongeInvalideStateException, CongeSoldeInsuffisantException, CongeAlreadyExistsException;
    boolean deleteConge(Long congeId) throws CongeNotFoundException;
    List<CongeDTO> getCongesByUtilisateur(Long utilisateurId);
    List<CongeDTO> getCongesByResponsable(Long responsableId);
    List<CongeDTO> searchCongeByUser(Long userId, String keyword);

    CongeDTO accepterConge(Long id) throws CongeNotFoundException, CongeInvalideStateException, UtilisateurNotFoundException;
    CongeDTO refuserConge(Long id) throws CongeNotFoundException, CongeInvalideStateException, UtilisateurNotFoundException;
    int CongePendingNb();
    int CongeApprovedNb();
    int CongeRefusedNb();
    List<CongeDTO> getCongesAccepted();

    CongeDTO uploadCongePdf(Long id, MultipartFile file) throws IOException, CongeNotFoundException;
    int getCongesByUtilisateurByEtat(long userId,String etat);
    int getNbCongesByUser(long userId);
    List<Integer> getCongesByMonthAndYear(int year);
    List<Integer> getCongesByMoisByUser(int idUser);
}
