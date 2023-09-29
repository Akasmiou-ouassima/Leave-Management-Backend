package com.nttdata.sid.dtos;

import com.nttdata.sid.enums.Etat;
import com.nttdata.sid.enums.Type;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CongeDTO {
    private Long id;
    private Date dateDebut;
    private Date dateFin;
    private Etat etat;
    private Type type;
    private String motif;
    private String fichier;
    private Long utilisateurId;
}