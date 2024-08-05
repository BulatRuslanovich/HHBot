package com.bipbup.utils;

import com.bipbup.dao.AppUserConfigDAO;
import com.bipbup.dao.AppUserDAO;
import com.bipbup.entity.AppUserConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class UserConfigUtil {
    private final AppUserConfigDAO appUserConfigDAO;

    public void updateConfigQuery(AppUserConfig config, String query) {
        config.setQueryText(query);
        appUserConfigDAO.save(config);
    }

    @Transactional
    public void deleteConfig(AppUserConfig config) {
        appUserConfigDAO.delete(config);
    }
}
