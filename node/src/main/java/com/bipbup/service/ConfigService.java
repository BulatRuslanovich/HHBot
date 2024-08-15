package com.bipbup.service;

import com.bipbup.entity.AppUser;
import com.bipbup.entity.AppUserConfig;
import com.bipbup.enums.impl.EducationLevelParam;

import java.util.List;
import java.util.Optional;

public interface ConfigService {
    List<AppUserConfig> getAll(int page, int size);

    AppUserConfig save(final AppUserConfig config);

    void delete(final AppUserConfig config);

    Optional<AppUserConfig> getById(final Long configId);

    List<AppUserConfig> getByUser(final AppUser user);

    Long saveConfigSelection(Long telegramId, Long configId);

    Long getSelectedConfigId(Long telegramId);

    void clearConfigSelection(Long telegramId);

    List<EducationLevelParam> addEducationLevelSelection(Long telegramId, EducationLevelParam param, List<EducationLevelParam> educationLevelParams);

    List<EducationLevelParam> removeEducationLevelSelection(Long telegramId, EducationLevelParam param, List<EducationLevelParam> educationLevelParams);

    List<EducationLevelParam> getSelectedEducationLevels(Long telegramId);

    void clearEducationLevelSelections(Long telegramId);
}
