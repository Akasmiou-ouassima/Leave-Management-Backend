package com.nttdata.sid.dtos;

import com.nttdata.sid.security.entities.AppUser;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EditProfileRequest {
    private UtilisateurDTO user;
    private String newPassword;

}
