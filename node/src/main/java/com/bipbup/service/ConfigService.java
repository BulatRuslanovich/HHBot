package com.bipbup.service;

import com.bipbup.entity.AppUser;
import com.bipbup.entity.AppUserConfig;
import com.bipbup.enums.impl.EducationLevelParam;
import com.bipbup.enums.impl.ScheduleTypeParam;

import java.util.List;
import java.util.Optional;

public interface ConfigService {

    List<AppUserConfig> getAll(int page, int size);

    AppUserConfig save(final AppUserConfig config);

    void delete(final AppUserConfig config);

    Optional<AppUserConfig> getById(final long configId);

    List<AppUserConfig> getByUser(final AppUser user);

    Long saveConfigSelection(long telegramId, long configId);

    Long getSelectedConfigId(long telegramId);

    void clearConfigSelection(long telegramId);

    List<EducationLevelParam> addEducationLevelSelection(long telegramId,
                                                         final EducationLevelParam param,
                                                         final List<EducationLevelParam> educationLevelParams);

    List<EducationLevelParam> removeEducationLevelSelection(long telegramId,
                                                            final EducationLevelParam param,
                                                            final List<EducationLevelParam> educationLevelParams);

    List<EducationLevelParam> getSelectedEducationLevels(long telegramId);

    void clearEducationLevelSelections(long telegramId);

    List<ScheduleTypeParam> addScheduleTypeSelection(long telegramId,
                                                     final ScheduleTypeParam param,
                                                     final List<ScheduleTypeParam> scheduleTypeParams);

    List<ScheduleTypeParam> removeScheduleTypeSelection(long telegramId,
                                                        final ScheduleTypeParam param,
                                                        final List<ScheduleTypeParam> scheduleTypeParams);

    List<ScheduleTypeParam> getSelectedScheduleTypes(long telegramId);

    void clearScheduleTypeSelections(long telegramId);
}
