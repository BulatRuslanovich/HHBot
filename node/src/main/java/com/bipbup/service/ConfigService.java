package com.bipbup.service;

import com.bipbup.entity.AppUser;
import com.bipbup.entity.AppUserConfig;
import com.bipbup.enums.impl.EducationLevelParam;
import com.bipbup.enums.impl.ScheduleTypeParam;

import java.util.List;
import java.util.Optional;

public interface ConfigService {

    List<AppUserConfig> getAll(int page, int size);

    AppUserConfig save(AppUserConfig config);

    void delete(AppUserConfig config);

    Optional<AppUserConfig> getById(long configId);

    List<AppUserConfig> getByUser(AppUser user);

    Long saveConfigSelection(long telegramId, long configId);

    Long getSelectedConfigId(long telegramId);

    void clearConfigSelection(long telegramId);

    List<EducationLevelParam> addEducationLevelSelection(long telegramId,
                                                         EducationLevelParam param,
                                                         List<EducationLevelParam> educationLevelParams);

    List<EducationLevelParam> removeEducationLevelSelection(long telegramId,
                                                            EducationLevelParam param,
                                                            List<EducationLevelParam> educationLevelParams);

    List<EducationLevelParam> getSelectedEducationLevels(long telegramId);

    void clearEducationLevelSelections(long telegramId);

    List<ScheduleTypeParam> addScheduleTypeSelection(long telegramId,
                                                     ScheduleTypeParam param,
                                                     List<ScheduleTypeParam> scheduleTypeParams);

    List<ScheduleTypeParam> removeScheduleTypeSelection(long telegramId,
                                                        ScheduleTypeParam param,
                                                        List<ScheduleTypeParam> scheduleTypeParams);

    List<ScheduleTypeParam> getSelectedScheduleTypes(long telegramId);

    void clearScheduleTypeSelections(long telegramId);
}
