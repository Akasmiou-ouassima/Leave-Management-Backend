package com.nttdata.sid.services;
import com.nttdata.sid.entities.Utilisateur;
import com.nttdata.sid.exceptions.UtilisateurNotFoundException;
import com.nttdata.sid.repositories.UtilisateurRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
public class EmailService {
    private final JavaMailSender javaMailSender;
    private UtilisateurRepository utilisateurRepository;
    private static final Logger logger = LoggerFactory.getLogger(EquipeServiceImpl.class);
    public EmailService(JavaMailSender javaMailSender, UtilisateurRepository utilisateurRepository) {
        this.javaMailSender = javaMailSender;
        this.utilisateurRepository = utilisateurRepository;
    }

    public void sendEmailRespo(Long utilisateurId, String msg) throws UtilisateurNotFoundException {
        Utilisateur utilisateur=utilisateurRepository.findById(utilisateurId).orElseThrow(() -> new UtilisateurNotFoundException("User not found with ID: " + utilisateurId));
        Utilisateur respo = utilisateur.getEquipe().getUtilisateur();
        logger.info("respo "+respo.getEmail());
        String emailContent = EmailContentGenerator.getEmailManagerContent(msg, respo, utilisateur);
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true);
            messageHelper.setTo(respo.getEmail());
            messageHelper.setSubject("Leave Request");
            messageHelper.setText(emailContent, true);
            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            logger.error("Erreur lors de l'envoi de l'e-mail à le responsable.", e);
        }
    }
    public void sendEmailEmploye(Long utilisateurId,String etat) throws UtilisateurNotFoundException {
        Utilisateur utilisateur=utilisateurRepository.findById(utilisateurId).orElseThrow(() -> new UtilisateurNotFoundException("User not found with ID: " + utilisateurId));
        String emailContent = EmailContentGenerator.getEmailEmployeeContent(etat, utilisateur);
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true);
            messageHelper.setTo(utilisateur.getEmail());
            messageHelper.setSubject("Response to Your Leave Request");
            messageHelper.setText(emailContent, true);
            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            logger.error("Erreur lors de l'envoi de l'e-mail à l'employé.", e);
        }
    }

    public void sendPasswordResetEmail(String userEmail, String token) throws UtilisateurNotFoundException {
        Utilisateur utilisateur = utilisateurRepository.findUtilisateurByEmail(userEmail).orElseThrow(() -> new UtilisateurNotFoundException("Utilisateur non trouvé avec l'e-mail: " + userEmail));

        if (utilisateur != null) {
            try {
                MimeMessage message = javaMailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

                String subject = "Reset Password";
                helper.setSubject(subject);
                helper.setTo(userEmail);

                String emailContent = EmailContentGenerator.getEmailContent(token,utilisateur);

                helper.setText(emailContent, true);
                helper.setSentDate(new Date());

                javaMailSender.send(message);

                logger.info("E-mail de réinitialisation de mot de passe envoyé à : " + userEmail);
            } catch (MessagingException ex) {
                logger.error("Erreur lors de l'envoi de l'e-mail de réinitialisation de mot de passe.", ex);
            }
        } else {
            logger.error("Impossible d'envoyer l'e-mail de réinitialisation de mot de passe. Utilisateur non trouvé.");
        }
    }


    public void sendEmailNouveauUser(Long utilisateurId,String password) throws UtilisateurNotFoundException {
        Utilisateur utilisateur=utilisateurRepository.findById(utilisateurId).orElseThrow(() -> new UtilisateurNotFoundException("User not found with ID: " + utilisateurId));

        String emailContent = EmailContentGenerator.getEmailNewUserContent(password, utilisateur);
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true);
            messageHelper.setTo(utilisateur.getEmail());
            messageHelper.setSubject("Welcome to REST QUEST");
            messageHelper.setText(emailContent, true);
            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            logger.error("Erreur lors de l'envoi de l'e-mail de d'ajout à l'application.", e);
        }
    }



}
