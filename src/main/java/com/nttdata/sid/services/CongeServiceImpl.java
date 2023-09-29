package com.nttdata.sid.services;

import com.nttdata.sid.dtos.CongeDTO;
import com.nttdata.sid.entities.Conge;
import com.nttdata.sid.entities.Equipe;
import com.nttdata.sid.entities.Utilisateur;
import com.nttdata.sid.enums.Etat;
import com.nttdata.sid.enums.Status;
import com.nttdata.sid.enums.Type;
import com.nttdata.sid.exceptions.*;
import com.nttdata.sid.mappers.MappersImpl;
import com.nttdata.sid.repositories.CongeRepository;
import com.nttdata.sid.repositories.EquipeRepository;
import com.nttdata.sid.repositories.UtilisateurRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class CongeServiceImpl implements CongeService {

    private CongeRepository congeRepository;
    private MappersImpl dtoMapper;

    private EmailService emailService;
    private EquipeRepository equipeRepository;
    private UtilisateurRepository utilisateurRepository;


    private static final Logger LOGGER = LoggerFactory.getLogger(CongeServiceImpl.class);

    public CongeServiceImpl(CongeRepository congeRepository, MappersImpl dtoMapper, EmailService emailService, EquipeRepository equipeRepository, UtilisateurRepository utilisateurRepository) {
        this.congeRepository = congeRepository;
        this.dtoMapper = dtoMapper;
        this.emailService = emailService;
        this.equipeRepository = equipeRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    @Override
    public CongeDTO save(CongeDTO congeDTO) throws UtilisateurNotFoundException, CongeAlreadyExistsException, CongeSoldeInsuffisantException {
        Conge conge = dtoMapper.fromCongeDTO(congeDTO);
        Utilisateur utilisateur = utilisateurRepository.findById(congeDTO.getUtilisateurId())
                .orElseThrow(() -> new UtilisateurNotFoundException("Utilisateur not found with ID: " + congeDTO.getUtilisateurId()));
        conge.setUtilisateur(utilisateur);
        List<Conge> conges = congeRepository.findCongeByUtilisateur(conge.getUtilisateur().getId());
        if (utilisateur.getEquipe().getUtilisateur().getStatus() == Status.DESACTIVE){
            throw new UtilisateurNotFoundException("The manager is on leave");
        }

        if (conges.stream().anyMatch(conge1 ->
                (
                        (conge1.getDateDebut().getYear() == conge.getDateDebut().getYear() &&
                                conge1.getDateDebut().getMonth() == conge.getDateDebut().getMonth() &&
                                conge1.getDateFin().getMonth() == conge.getDateFin().getMonth() &&
                                conge1.getDateFin().getYear() == conge.getDateFin().getYear())
                                &&
                                (
                                        (conge1.getDateDebut().getDate() == conge.getDateDebut().getDate() ||
                                                conge1.getDateFin().getDate() == conge.getDateFin().getDate())
                                                ||
                                                (conge1.getDateDebut().getDate() < conge.getDateDebut().getDate() &&
                                                        conge1.getDateFin().getDate() > conge.getDateFin().getDate())
                                                ||
                                                (conge1.getDateDebut().getDate() < conge.getDateDebut().getDate() &&
                                                        conge.getDateDebut().getDate() <= conge1.getDateFin().getDate())
                                                ||
                                                (conge1.getDateDebut().getDate() <= conge.getDateFin().getDate() &&
                                                        conge.getDateFin().getDate() < conge1.getDateFin().getDate())
                                                ||
                                                (conge1.getDateDebut().getDate() > conge.getDateDebut().getDate() &&
                                                        conge.getDateFin().getDate() > conge1.getDateFin().getDate())
                                )
                ))) {
            throw new CongeAlreadyExistsException("Conge already exists");
        } else {
            LOGGER.info("Saving new Conge");
            int solde = conge.getUtilisateur().getSolde();
            long diff = conge.getDateFin().getTime() - conge.getDateDebut().getTime();
            int nbJours = (int) (diff / (1000 * 60 * 60 * 24) + 1);
            conge.setEtat(Etat.EN_ATTENTE);
            if (conge.getType() == Type.PAYE && nbJours <= solde) {
                Conge savedConge = congeRepository.save(conge);
                savedConge.getUtilisateur().setSolde(solde - nbJours);
                utilisateurRepository.save(savedConge.getUtilisateur());
                CongeDTO savedCongeDTO = dtoMapper.fromConge(savedConge);
                LOGGER.info("Leave saved successfully with id {}", savedCongeDTO.getId());
                // Start sending the email in the background without blocking the main thread
                CompletableFuture.runAsync(() -> {
                    try {
                        emailService.sendEmailRespo(congeDTO.getUtilisateurId(), "request");
                        LOGGER.info("email sent create");
                    } catch (Exception e) {
                        LOGGER.error("Failed to send email to employee: {}", e.getMessage());
                    }
                });
                return savedCongeDTO;
            } else if (conge.getType() == Type.PAYE && nbJours > solde) {
                throw new CongeSoldeInsuffisantException("Solde insuffisant");
            } else {
                Conge savedConge = congeRepository.save(conge);
                CongeDTO savedCongeDTO = dtoMapper.fromConge(savedConge);
                LOGGER.info("Leave saved successfully with id {}", savedCongeDTO.getId());
                CompletableFuture.runAsync(() -> {
                    try {
                        emailService.sendEmailRespo(congeDTO.getUtilisateurId(), "request");
                        LOGGER.info("email sent create");
                    } catch (Exception e) {
                        LOGGER.error("Failed to send email to employee: {}", e.getMessage());
                    }
                });
                return savedCongeDTO;
            }
        }
    }

    @Override
    public List<CongeDTO> listConges() {
        LOGGER.info("Fetching list of Leaves");
        List<Conge> conges = congeRepository.findAll();
        List<CongeDTO> congeDTOS = conges.stream().map(dtoMapper::fromConge).collect(Collectors.toList());
        LOGGER.info("Fetched {} Leaves", conges.size());
        return congeDTOS;
    }

    @Override
    public CongeDTO getConge(Long congeId) throws CongeNotFoundException {
        LOGGER.info("Fetching Leave with ID: {}", congeId);
        Conge conge = congeRepository.findById(congeId).orElseThrow(() -> new CongeNotFoundException("Leave not found with this id: " + congeId));
        CongeDTO congeDTO = dtoMapper.fromConge(conge);
        LOGGER.info("Fetched Leave with ID: {}", congeId);
        return congeDTO;
    }

    @Override
    public CongeDTO updateConge(CongeDTO congeDTO) throws CongeNotFoundException, UtilisateurNotFoundException, CongeInvalideStateException, CongeSoldeInsuffisantException, CongeAlreadyExistsException {
        LOGGER.info("Updating Conge with ID: {}", congeDTO.getId());
        Conge conge = dtoMapper.fromCongeDTO(congeDTO);
        Utilisateur utilisateur = utilisateurRepository.findById(congeDTO.getUtilisateurId())
                .orElseThrow(() -> new UtilisateurNotFoundException("User not found with ID: " + congeDTO.getUtilisateurId()));
        List<Conge> conges = congeRepository.findCongeByUtilisateur(congeDTO.getUtilisateurId());
        if (utilisateur.getEquipe().getUtilisateur().getStatus() == Status.DESACTIVE){
            throw new UtilisateurNotFoundException("The manager is on leave");
        }
        if (conges.stream().anyMatch(conge1 ->
                (
                        (congeDTO.getId()!=conge1.getId())
                                &&
                                (conge1.getDateDebut().getYear() == conge.getDateDebut().getYear() &&
                                        conge1.getDateDebut().getMonth() == conge.getDateDebut().getMonth() &&
                                        conge1.getDateFin().getMonth() == conge.getDateFin().getMonth() &&
                                        conge1.getDateFin().getYear() == conge.getDateFin().getYear()
                                )
                                &&
                                (
                                        (conge1.getDateDebut().getDate() == conge.getDateDebut().getDate() ||
                                                conge1.getDateFin().getDate() == conge.getDateFin().getDate())
                                                ||
                                                (conge1.getDateDebut().getDate() < conge.getDateDebut().getDate() &&
                                                        conge1.getDateFin().getDate() > conge.getDateFin().getDate())
                                                ||
                                                (conge1.getDateDebut().getDate() < conge.getDateDebut().getDate() &&
                                                        conge.getDateDebut().getDate() <= conge1.getDateFin().getDate())
                                                ||
                                                (conge1.getDateDebut().getDate() <= conge.getDateFin().getDate() &&
                                                        conge.getDateFin().getDate() < conge1.getDateFin().getDate())
                                                ||
                                                (conge1.getDateDebut().getDate() > conge.getDateDebut().getDate() &&
                                                        conge.getDateFin().getDate() > conge1.getDateFin().getDate())
                                )
                )) )
        {
            throw new CongeAlreadyExistsException("Conge already exists");
        }
        Conge congeBD = congeRepository.findById(congeDTO.getId()).orElseThrow(() -> new CongeNotFoundException("Leave not found with this id: " + congeDTO.getId()));

        if (congeBD.getType() == Type.PAYE) {
            Date dateDebut = congeBD.getDateDebut();
            Date dateFin = congeBD.getDateFin();
            long solde1 = dateFin.getTime() - dateDebut.getTime() ;
            int nbJours1 = (int) (solde1 / (1000 * 60 * 60 * 24) + 1);
            congeBD.getUtilisateur().setSolde(congeBD.getUtilisateur().getSolde() + nbJours1);
        }

        conge.setUtilisateur(utilisateur);
        if (congeBD.getEtat() != Etat.EN_ATTENTE) {
            throw new CongeInvalideStateException("The leave status must be Pending");
        }

        int solde = congeBD.getUtilisateur().getSolde();
        long diff = conge.getDateFin().getTime() - conge.getDateDebut().getTime();
        int nbJours = (int) (diff / (1000 * 60 * 60 * 24) +1);

        if (conge.getType() == Type.PAYE && nbJours <= solde) {
            Conge updatedConge = congeRepository.save(conge);
            updatedConge.getUtilisateur().setSolde(solde - nbJours);
            CongeDTO updatedCongeDTO = dtoMapper.fromConge(updatedConge);
            LOGGER.info("Leave updated successfully (paye) with ID: {}", congeDTO.getId());
            CompletableFuture.runAsync(() -> {
                try {
                    emailService.sendEmailRespo(congeDTO.getUtilisateurId(), "modification");
                    LOGGER.info("email sent update");
                } catch (Exception e) {
                    LOGGER.error("Failed to send email to employee: {}", e.getMessage());
                }
            });
            return updatedCongeDTO;
        } else if (conge.getType() == Type.PAYE && nbJours > solde) {
            LOGGER.info("Leave number of days is not accepted");
            throw new CongeSoldeInsuffisantException("Solde insuffisant");
        } else {
            Conge updatedConge = congeRepository.save(conge);
            CongeDTO updatedCongeDTO = dtoMapper.fromConge(updatedConge);
            CompletableFuture.runAsync(() -> {
                try {
                    LOGGER.info("Leave updated successfully with ID: {}", congeDTO.getId());
                    emailService.sendEmailRespo(congeDTO.getUtilisateurId(), "modification");
                    LOGGER.info("email sent");
                } catch (Exception e) {
                    LOGGER.error("Failed to send email to employee: {}", e.getMessage());
                }
            });
            return updatedCongeDTO;
        }
    }

    @Override
    public boolean deleteConge(Long congeId) throws CongeNotFoundException {
        Conge conge = congeRepository.findById(congeId).orElseThrow(() -> new CongeNotFoundException("Leave not found with this id: " + congeId));
        if ((conge.getEtat().equals(Etat.EN_ATTENTE) || conge.getDateDebut().after(new Date())) && !conge.getEtat().equals(Etat.REFUSE)) {
            if (conge.getType() == Type.PAYE) {
                LOGGER.info(" ---conge de type payée--- ");
                int solde = conge.getUtilisateur().getSolde();
                long diff = conge.getDateFin().getTime() - conge.getDateDebut().getTime();
                int nbJours = (int) (diff / (1000 * 60 * 60 * 24) +1);
                conge.getUtilisateur().setSolde(solde + nbJours);
                LOGGER.info("solde pre " + solde + " nbj " + nbJours + " post solde " + conge.getUtilisateur().getSolde());
                utilisateurRepository.save(conge.getUtilisateur());
                LOGGER.info(" ---save solde user--- ");
            }

            LOGGER.info("Deleting Leave with ID: {}", congeId);
            congeRepository.deleteById(congeId);
            LOGGER.info("Leave deleted successfully with ID: {}", congeId);
            return true;
        } else {
            LOGGER.info("Leave can't be deleted because it's not in the 'Pending' state");
            return false;
        }
    }

    @Override
    public List<CongeDTO> getCongesByUtilisateur(Long utilisateurId) {
        List<Conge> conges = congeRepository.findCongesByUtilisateurId(utilisateurId);
        return conges.stream().map(dtoMapper::fromConge).collect(Collectors.toList());
    }

    @Override
    public List<CongeDTO> getCongesByResponsable(Long responsableId) {
        List<Equipe> equipes = equipeRepository.findEquipesByUtilisateurId(responsableId);
        List<Utilisateur> utilisateurs = equipes.stream().flatMap(equipe -> equipe.getUtilisateurs().stream()).collect(Collectors.toList());
        List<Conge> conges = utilisateurs.stream().flatMap(utilisateur -> congeRepository.findCongesByUtilisateurId(utilisateur.getId()).stream()).collect(Collectors.toList());
        return conges.stream().map(dtoMapper::fromConge).collect(Collectors.toList());
    }

    @Override
    public List<CongeDTO> searchCongeByUser(Long userId, String keyword) {
        LOGGER.info("Searching Leave for User with keyword: {}", keyword);
        List<Conge> conges = congeRepository.findCongeByUserAndKeyword(userId, keyword);
        List<CongeDTO> congeDTOS = conges.stream().map(dtoMapper::fromConge).collect(Collectors.toList());
        LOGGER.info("Found {} Conges for User with keyword: {}", conges.size(), keyword);
        return congeDTOS;
    }

    @Override
    public CongeDTO accepterConge(Long id) throws CongeNotFoundException, CongeInvalideStateException, UtilisateurNotFoundException {
        Conge conge = congeRepository.findById(id).orElseThrow(() -> new CongeNotFoundException("Leave not found with this id: " + id));
        if (conge.getEtat() == Etat.EN_ATTENTE) {
            conge.setEtat(Etat.ACCEPTE);
            Conge acceptedConge = congeRepository.save(conge);
            CongeDTO congeDTOSaved = dtoMapper.fromConge(acceptedConge);
            LOGGER.info("Leave accepted successfully with ID: {}", congeDTOSaved.getId());
            CompletableFuture.runAsync(() -> {
                try {
                    emailService.sendEmailEmploye(congeDTOSaved.getUtilisateurId(), "approved");
                    LOGGER.info("email sent to employee");
                } catch (Exception e) {
                    LOGGER.error("Failed to send email to employee: {}", e.getMessage());
                }
            });
            return congeDTOSaved;
        } else {
            throw new CongeInvalideStateException("The leave status must be 'Pending'");
        }
    }

    @Override
    public CongeDTO refuserConge(Long id) throws CongeNotFoundException, CongeInvalideStateException, UtilisateurNotFoundException {
        Conge conge = congeRepository.findById(id).orElseThrow(() -> new CongeNotFoundException("Leave not found with this id: " + id));
        if (conge.getEtat() == Etat.EN_ATTENTE) {
            conge.setEtat(Etat.REFUSE);

            if (conge.getType() == Type.PAYE) {
                int solde = conge.getUtilisateur().getSolde();
                long diff = conge.getDateFin().getTime() - conge.getDateDebut().getTime();
                int nbJours = (int) (diff / (1000 * 60 * 60 * 24) +1);
                conge.getUtilisateur().setSolde(solde + nbJours);
                LOGGER.info("solde pre " + solde + " nbj " + nbJours + " post solde " + conge.getUtilisateur().getSolde());
            }

            Conge congeSaved = congeRepository.save(conge);
            CongeDTO congeDTOSaved = dtoMapper.fromConge(congeSaved);
            LOGGER.info("Leave rejected successfully with ID: {}", congeDTOSaved.getId());
            CompletableFuture.runAsync(() -> {
                try {
                    emailService.sendEmailEmploye(congeDTOSaved.getUtilisateurId(), "rejected");
                    LOGGER.info("email sent to employee");
                } catch (Exception e) {
                    LOGGER.error("Failed to send email to employee: {}", e.getMessage());
                }
            });
            return congeDTOSaved;
        } else {
            throw new CongeInvalideStateException("The leave status must be 'Pending'");
        }
    }

    @Override
    public int CongePendingNb() {
        List<Conge> conges = congeRepository.findCongesByEtat(Etat.EN_ATTENTE);
        return conges.size();
    }

    @Override
    public int CongeApprovedNb() {
        List<Conge> conges = congeRepository.findCongesByEtat(Etat.ACCEPTE);
        return conges.size();
    }

    @Override
    public int CongeRefusedNb() {
        List<Conge> conges = congeRepository.findCongesByEtat(Etat.REFUSE);
        return conges.size();
    }

    @Override
    public List<CongeDTO> getCongesAccepted() {
        List<Conge> conges = congeRepository.findCongesByEtat(Etat.ACCEPTE);
        List<CongeDTO> congeDTOS = conges.stream().map(dtoMapper::fromConge).collect(Collectors.toList());
        return congeDTOS;
    }

    @Override
    public CongeDTO uploadCongePdf(Long id, MultipartFile file) throws IOException, CongeNotFoundException {
        Conge conge = congeRepository.findById(id).orElseThrow(() -> new CongeNotFoundException("Leave not found with this id: " + id));

        if (!isValidPdfFileType(file.getContentType())) {
            throw new IllegalArgumentException("Unsupported file type. Only PDF files are allowed.");
        }
        // Generate a unique file name to avoid conflicts
        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();

        String DIRECTORY = "C:\\Users\\ouasi\\Downloads\\Leave-Management-Frontend\\src\\assets\\img\\leaves";
        Path fileStorage = Paths.get(DIRECTORY, filename).toAbsolutePath().normalize();

        try {
            // Create the directory if it doesn't exist
            Files.createDirectories(fileStorage.getParent());

            // Copy the file to the target path
            Files.copy(file.getInputStream(), fileStorage, StandardCopyOption.REPLACE_EXISTING);

            // Update the 'conge' with the PDF file path
            conge.setFichier(filename);
            congeRepository.save(conge);
        } catch (IOException e) {
            throw new IOException("Failed to save the file. Please try again later.", e);
        }
        return dtoMapper.fromConge(conge);
    }

    private boolean isValidPdfFileType(String contentType) {
        return contentType != null && contentType.equals(MediaType.APPLICATION_PDF_VALUE);
    }

    @Override
    public int getCongesByUtilisateurByEtat(long userId, String etat) {
        List<Conge> congesByEtat = new ArrayList<>();
        List<Conge> conges = congeRepository.findCongesByUtilisateurId(userId);
        for (Conge c : conges) {
            if (c.getEtat() == Etat.valueOf(etat)) {
                congesByEtat.add(c);
            }
        }
        return congesByEtat.size();
    }

    @Override
    public int getNbCongesByUser(long userId) {
        int total;
        total = getCongesByUtilisateur(userId).size();
        return total;
    }

    @Override
    public List<Integer> getCongesByMonthAndYear(int year) {

        List<Conge> allCongesAcceptes = congeRepository.findCongesByEtat(Etat.valueOf("ACCEPTE"));
        List<Conge> allCongesRefuses = congeRepository.findCongesByEtat(Etat.valueOf("REFUSE"));
        int nbr = 0;
        List<Integer> values = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            for (Conge conge : allCongesAcceptes) {
                Date dateDebut = conge.getDateDebut();
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                String dateStart = formatter.format(dateDebut);
                LocalDate date = LocalDate.parse(dateStart);
                int annee = date.getYear();
                Month mois = date.getMonth();

                if (mois.getValue() == i && annee == year) {
                    nbr++;
                }
            }
            System.out.println("mois " + i + " => nbrAccept1 :  " + nbr);
            values.add(nbr);
            nbr = 0;
        }

        System.out.println("------------------------------");
        nbr = 0;
        for (int i = 1; i <= 12; i++) {
            for (Conge conge : allCongesRefuses) {
                Date dateDebut = conge.getDateDebut();
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                String dateStart = formatter.format(dateDebut);
                LocalDate date = LocalDate.parse(dateStart);
                int annee = date.getYear();
                Month mois = date.getMonth();

                if (mois.getValue() == i && annee == year) {
                    nbr++;
                }
            }
            System.out.println("mois " + i + " => nbrRefus1 : " + nbr);
            values.add(nbr);
            nbr = 0;
        }
        System.out.println("values  " + values);
        return values;
    }

    @Override
    public List<Integer> getCongesByMoisByUser(int idUser) {
        LocalDate currentDate = LocalDate.now();
        int currentYear = currentDate.getYear();
        List<Conge> allCongesAcceptes = congeRepository.findCongesByEtat(Etat.valueOf("ACCEPTE"));
        List<Conge> allCongesRefuses = congeRepository.findCongesByEtat(Etat.valueOf("REFUSE"));
        int nbr = 0;
        List<Integer> values = new ArrayList<>();
        List<Conge> congesUserAcceptes = new ArrayList<>();
        List<Conge> congesUserRefuses = new ArrayList<>();

        for (Conge c : allCongesAcceptes) {
            if (c.getUtilisateur().getId() == idUser) {
                congesUserAcceptes.add(c);
            }
        }
        for (Conge c : allCongesRefuses) {
            if (c.getUtilisateur().getId() == idUser) {
                congesUserRefuses.add(c);
            }
        }
        for (int i = 1; i <= 12; i++) {
            for (Conge conge : congesUserAcceptes) {
                Date dateDebut = conge.getDateDebut();
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                String dateStart = formatter.format(dateDebut);
                LocalDate date = LocalDate.parse(dateStart);
                int annee = date.getYear();
                Month mois = date.getMonth();
                if (mois.getValue() == i && annee == currentYear) {
                    nbr++;
                }
            }
            System.out.println("mois " + i + " => nbrAccept " + nbr);
            values.add(nbr);
            nbr = 0;
        }

        System.out.println("--------------------------- ");
        nbr = 0;
        for (int i = 1; i <= 12; i++) {
            for (Conge conge : congesUserRefuses) {
                Date dateDebut = conge.getDateDebut();
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                String dateStart = formatter.format(dateDebut);
                LocalDate date = LocalDate.parse(dateStart);
                int annee = date.getYear();
                Month mois = date.getMonth();
                if (mois.getValue() == i && annee == currentYear) {
                    nbr++;
                }
            }
            System.out.println("mois " + i + " => nbrRefus " + nbr);
            values.add(nbr);
            nbr = 0;
        }
        System.out.println("values user " + values);
        return values;
    }
    @Scheduled(cron = "0 1 0 * * ?")
    public void verifierDemandesDeConge() throws CongeNotFoundException, CongeInvalideStateException, UtilisateurNotFoundException {
        LocalDate dateDuJour = LocalDate.now();
        System.out.println("day "+dateDuJour.getDayOfYear()+" month "+dateDuJour.getMonth()+" year "+dateDuJour.getYear());
        List<Conge> demandes = congeRepository.findCongesByEtat(Etat.valueOf("EN_ATTENTE"));;
        for (Conge conge : demandes) {
            Date dateDebut = conge.getDateDebut();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            String dateStart = formatter.format(dateDebut);
            LocalDate date = LocalDate.parse(dateStart);
            int annee = date.getYear();
            Month mois = date.getMonth();
            int day = date.getDayOfYear();
            if (day== dateDuJour.getDayOfYear() && mois==dateDuJour.getMonth() && annee==dateDuJour.getYear()) {
                System.out.println("condition vérifié id =>"+conge.getId());
                refuserConge(conge.getId());
            }
        }
    }

}

