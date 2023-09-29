package com.nttdata.sid.security.repositories;


import com.nttdata.sid.security.entities.AppUser;
import net.datafaker.providers.base.App;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    AppUser findAppUserByEmail(String email);
    AppUser findAppUserById(Long id);
}
