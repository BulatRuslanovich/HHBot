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

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
public class UserConfigUtil {
    private final AppUserConfigDAO appUserConfigDAO;
    private final AppUserDAO appUserDAO;

    public void updateConfigQuery(final AppUserConfig config,
                                  final String query) {
        config.setQueryText(query);
        appUserConfigDAO.save(config);
        log.info("Updated query for config {}: {}",
                config.getConfigName(),
                query);
    }

    public Optional<AppUserConfig> getConfigById(final long configId) {
        return appUserConfigDAO.findById(configId);
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void removeConfig(final AppUserConfig config) {
        try {
            AppUser user = config.getAppUser();
            user.getAppUserConfigs().remove(config);
            appUserDAO.save(user);
            appUserConfigDAO.delete(config);
            log.info("Removed config {} from user {}",
                    config.getConfigName(),
                    user.getFirstName());
        } catch (Exception e) {
            log.error("Error removing config {}: {}",
                    config.getConfigName(),
                    e.getMessage(), e);
            throw e;
        }
    }
}
