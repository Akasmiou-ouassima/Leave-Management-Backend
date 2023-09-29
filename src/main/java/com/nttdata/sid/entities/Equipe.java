package com.nttdata.sid.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data @NoArgsConstructor @AllArgsConstructor
@Builder
public class Equipe {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nom;
    private String description;
    private String image;

    // Une equipe constitué de plusieurs membres
    @OneToMany(mappedBy = "equipe",fetch = FetchType.LAZY)
    private List<Utilisateur> utilisateurs=new ArrayList<>();

    // Une équipe peut etre géré par un seul responsable
    @ManyToOne
    private Utilisateur utilisateur;
}
