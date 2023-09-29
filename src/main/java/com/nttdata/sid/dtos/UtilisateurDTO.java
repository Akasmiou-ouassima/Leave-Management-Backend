package com.nttdata.sid.dtos;

import com.nttdata.sid.enums.Status;
import com.nttdata.sid.enums.UserType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data @NoArgsConstructor
@AllArgsConstructor
@Builder
public class UtilisateurDTO {
    private Long id;
    private String nom;
    private String prenom;
    private Status status;
    private String email;
    private String poste;
    private UserType type ;
    private String tel;
    private String image;
    private String adresse;
    private int solde;
    private Long equipeId;
    private List<Long> equipesGereesIds;
}