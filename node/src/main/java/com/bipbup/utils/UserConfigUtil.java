package com.bipbup.utils;

import com.bipbup.dao.AppUserConfigDAO;
import com.bipbup.dao.AppUserDAO;
import com.bipbup.entity.AppUser;
import com.bipbup.entity.AppUserConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Component
public class UserConfigUtil {
    private final AppUserConfigDAO appUserConfigDAO;
    private final AppUserDAO appUserDAO;

    public void updateConfigQuery(AppUserConfig config, String query) {
        config.setQueryText(query);
        appUserConfigDAO.save(config);
        log.info("Updated query for config {}: {}", config.getConfigName(), query);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void removeConfig(AppUserConfig config) {
        try {
            AppUser appUser = config.getAppUser();
            appUser.getAppUserConfigs().remove(config);
            appUserDAO.save(appUser);
            appUserConfigDAO.delete(config);
            log.info("Removed config {} from user {}", config.getConfigName(), appUser.getFirstName());
        } catch (Exception e) {
            log.error("Error removing config {}: {}", config.getConfigName(), e.getMessage(), e);
            throw e;
        }
    }
}
