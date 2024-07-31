package com.bipbup.dao;

import com.bipbup.entity.AppUser;
import com.bipbup.entity.AppUserConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserConfigDAO extends JpaRepository<AppUserConfig, Long> {
    Optional<AppUserConfig> findByAppUser(AppUser appUser);
}
