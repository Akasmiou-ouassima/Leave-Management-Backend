package com.nttdata.sid.helper;

import com.nttdata.sid.dtos.CongeDTO;
import com.nttdata.sid.dtos.EquipeDTO;
import com.nttdata.sid.dtos.UtilisateurDTO;
import com.nttdata.sid.entities.Conge;
import com.nttdata.sid.entities.Equipe;
import com.nttdata.sid.entities.Utilisateur;
import com.nttdata.sid.enums.Etat;
import com.nttdata.sid.enums.Type;
import com.nttdata.sid.enums.UserType;
import com.nttdata.sid.enums.Status;
import com.nttdata.sid.mappers.MappersImpl;
import com.nttdata.sid.repositories.CongeRepository;
import com.nttdata.sid.repositories.EquipeRepository;
import com.nttdata.sid.repositories.UtilisateurRepository;
import com.nttdata.sid.security.entities.AppRole;
import com.nttdata.sid.security.entities.AppUser;
import com.nttdata.sid.security.repositories.AppRoleRepository;
import com.nttdata.sid.security.repositories.AppUserRepository;
import com.nttdata.sid.security.services.ISecurityService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@AllArgsConstructor
@Component
public class Seeder implements CommandLineRunner {
    private final UtilisateurRepository utilisateurRepository;
    private final EquipeRepository equipeRepository;
    private final CongeRepository congeRepository;
    private final AppRoleRepository appRoleRepository;
    private ISecurityService iSecurityService;

    @Override
    public void run(String... args) throws Exception {

    }
   /* private final Faker faker = new Faker();

    @Override
    public void run(String... args) throws Exception {
        List<AppRole> roles=new ArrayList<>();
        roles.add(AppRole.builder().roleName("ADMIN").build());
        roles.add(AppRole.builder().roleName("SALARIE").build());
        roles.add(AppRole.builder().roleName("RESPONSABLE").build());
        appRoleRepository.saveAll(roles);
        System.out.println("roles added");
        // Seeder les utilisateurs
        List<Utilisateur> utilisateurs = IntStream.rangeClosed(1, 10).mapToObj(
                i -> Utilisateur.builder()
                        .nom(faker.name().firstName())
                        .prenom(faker.name().lastName())
                        .status(faker.options().option(Status.ACTIVE, Status.DESACTIVE))
                        .email(faker.internet().emailAddress())
                        .poste(faker.job().title())
                        .type(faker.options().option(UserType.SALARIE, UserType.RESPONSABLE))
                        .tel(faker.phoneNumber().phoneNumber())
                        .image("image.png")
                        .adresse(faker.address().fullAddress())
                        .solde(faker.number().numberBetween(0, 18))
                        .conges(new ArrayList<>())
                        .build()
        ).toList();

        log.info("Saving {} utilisateurs", utilisateurs.size());
        utilisateurRepository.saveAll(utilisateurs);
        log.info("Done saving utilisateurs");

        // Seeder les équipes
        List<Equipe> equipes = IntStream.rangeClosed(1, 5).mapToObj(
                i -> Equipe.builder()
                        .nom(faker.team().name())
                        .description(faker.lorem().sentence())
                        .image("6d7077f3-bd8a-43fa-beb8-32b3bfc00f0a_F4.jpeg")
                        .utilisateurs(new ArrayList<>())
                        .build()
        ).toList();

        log.info("Saving {} équipes", equipes.size());
        equipeRepository.saveAll(equipes);
        log.info("Done saving équipes");

        utilisateurs.forEach(utilisateur -> {
            Equipe equipe = equipes.get(faker.number().numberBetween(0, equipes.size()));
            utilisateur.setEquipe(equipe);
            equipe.getUtilisateurs().add(utilisateur);
        });


        utilisateurRepository.saveAll(utilisateurs);


        equipes.forEach(equipe -> {
            Utilisateur utilisateur = utilisateurs.get(faker.number().numberBetween(0, utilisateurs.size()));
            equipe.setUtilisateur(utilisateur);
        });

        equipeRepository.saveAll(equipes);
        // Seeder les congés
        List<Conge> conges = IntStream.rangeClosed(1, 20).mapToObj(i -> {
            // Generate random dates for dateDebut and dateFin
            Date dateDebut = faker.date().future(30, TimeUnit.DAYS);
            Date dateFin = faker.date().future(30, TimeUnit.DAYS);

            // Swap dates if dateDebut is after dateFin
            if (dateDebut.after(dateFin)) {
                Date temp = dateDebut;
                dateDebut = dateFin;
                dateFin = temp;
            }
            return Conge.builder()
                    .dateDebut(dateDebut)
                    .dateFin(dateFin)
                    .etat(Etat.values()[faker.number().numberBetween(0, Etat.values().length)])
                    .type(Type.values()[faker.number().numberBetween(0, Type.values().length)])
                    .motif(faker.lorem().sentence())
                    .fichier(faker.file().fileName())
                    .utilisateur(utilisateurs.get(faker.number().numberBetween(0, utilisateurs.size())))
                    .build();
        }).collect(Collectors.toList());

        log.info("Saving {} congés", conges.size());
        congeRepository.saveAll(conges);
        log.info("Done saving congés");

        for (int i= 0; i < utilisateurs.size(); i++) {
            AppUser appUser = new AppUser();
            appUser.setPassword("1234");
            appUser.setEmail(utilisateurs.get(i).getEmail());
            Collection<AppRole> roles1 =new ArrayList<>();
            if (i==0){
                AppRole appRole = iSecurityService.findRoleByRoleName("ADMIN");
                roles1.add(appRole);
                appUser.setUserRoles(roles1);
            }else {
                AppRole appRole = iSecurityService.findRoleByRoleName("SALARIE");
                AppRole appRole1 = iSecurityService.findRoleByRoleName("RESPONSABLE");
                if (Math.random() < 0.5) {
                    roles1.add(appRole);
                } else {
                    roles1.add(appRole1);
                }
                appUser.setUserRoles(roles1);

            }
            iSecurityService.addNewUser(appUser);
        }

    }*/

}
