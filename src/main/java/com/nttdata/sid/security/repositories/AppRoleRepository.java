package com.nttdata.sid.security.repositories;


import com.nttdata.sid.security.entities.AppRole;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppRoleRepository extends JpaRepository<AppRole, Long> {
    AppRole findAppRoleByRoleName(String roleName);
}
