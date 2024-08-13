package com.bipbup.dao;

import com.bipbup.entity.AppUser;
import com.bipbup.entity.AppUserConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppUserConfigDAO extends JpaRepository<AppUserConfig, Long> {
    List<AppUserConfig> findByAppUser(AppUser appUser);
}
