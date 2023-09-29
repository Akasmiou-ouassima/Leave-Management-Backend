package com.nttdata.sid.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data @NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipeDTO {
    private Long id;
    private String nom;
    private String description;
    private String image;
    private Long responsableId;
}
