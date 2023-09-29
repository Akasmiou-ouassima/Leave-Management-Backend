package com.nttdata.sid.entities;

import com.nttdata.sid.enums.Etat;
import com.nttdata.sid.enums.Type;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Temporal(TemporalType.DATE)
    private Date dateDebut;
    @Temporal(TemporalType.DATE)
    private Date dateFin;
    @Enumerated(EnumType.STRING)
    private Etat etat;
    @Enumerated(EnumType.STRING)
    private Type type;
    private String motif;
    private String fichier;

    // Un congé est fait par un seul utilisateur
    @ManyToOne
    private Utilisateur utilisateur;
}
