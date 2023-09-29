package com.nttdata.sid.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nttdata.sid.enums.Status;
import com.nttdata.sid.enums.UserType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data  @AllArgsConstructor
@NoArgsConstructor
@Builder
public class Utilisateur {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nom;
    private String prenom;
    @Enumerated(EnumType.STRING)
    private Status status;
    @Column(unique = true)
    private String email;
    private String poste;
    @Enumerated(EnumType.STRING)
    private UserType type ;
    private String tel;
    private String image;
    private String adresse;
    private int solde;

    // un user (SALARIE) appartient à un seul équipe
    @ManyToOne
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Equipe equipe;

    // Un responsable peut gérer plusieurs équipes
    @OneToMany(mappedBy = "utilisateur",fetch = FetchType.LAZY)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<Equipe> equipes=new ArrayList<>();

    // Un utilisateur peut demander plusieurs congés
    @OneToMany(mappedBy = "utilisateur",cascade = CascadeType.REMOVE)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<Conge> conges=new ArrayList<>();
}
