package com.bipbup.dao;

import com.bipbup.entity.AppUser;
import com.bipbup.entity.AppUserConfig;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppUserConfigDAO extends JpaRepository<AppUserConfig, Long> {

    List<AppUserConfig> findByAppUser(AppUser appUser);

    void deleteAllByAppUser(AppUser appUser);

    Long countAppUserConfigByAppUser(AppUser appUser);
}
