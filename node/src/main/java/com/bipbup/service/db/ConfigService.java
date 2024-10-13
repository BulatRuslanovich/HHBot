package com.bipbup.service.db;

import com.bipbup.entity.AppUser;
import com.bipbup.entity.AppUserConfig;
import java.util.List;
import java.util.Optional;

public interface ConfigService {

    List<AppUserConfig> getConfigsFromPage(int numOfPage, int sizeOfPage);

    AppUserConfig saveConfig(AppUserConfig config, boolean updateParams);

    void deleteConfig(AppUserConfig config);

    Optional<AppUserConfig> getConfigById(long id);

    List<AppUserConfig> getConfigByUser(AppUser user);

    Long countOfConfigs(AppUser user);
}
