package com.bipbup.service.impl;

import com.bipbup.dao.AppUserConfigDAO;
import com.bipbup.entity.AppUser;
import com.bipbup.entity.AppUserConfig;
import com.bipbup.enums.impl.EducationLevelParam;
import com.bipbup.enums.impl.ScheduleTypeParam;
import com.bipbup.service.ConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class ConfigServiceImpl implements ConfigService {

    private final AppUserConfigDAO appUserConfigDAO;

    @Override
    public List<AppUserConfig> getAll(int page, int size) {
        var pageRequest = PageRequest.of(page, size);
        var pageResult = appUserConfigDAO.findAll(pageRequest);
        return pageResult.toList();
    }

    @Override
    public AppUserConfig save(AppUserConfig config) {
        return appUserConfigDAO.saveAndFlush(config);
    }

    @Override
    public void delete(AppUserConfig config) {
        appUserConfigDAO.delete(config);
    }

    @Override
    public Optional<AppUserConfig> getById(long configId) {
        return appUserConfigDAO.findById(configId);
    }

    @Override
    public List<AppUserConfig> getByUser(AppUser user) {
        return appUserConfigDAO.findByAppUser(user);
    }

    @Override
    @CachePut(value = "configSelections", key = "#telegramId")
    public Long saveConfigSelection(long telegramId, long configId) {
        return configId;
    }

    @Override
    @Cacheable(value = "configSelections")
    public Long getSelectedConfigId(long telegramId) {
        return null;
    }

    @Override
    @CacheEvict(value = "configSelections")
    public void clearConfigSelection(long telegramId) {
        // clearing cache, doesn't need implementing
    }

    @Override
    @CachePut(value = "educationSelections", key = "#telegramId")
    public List<EducationLevelParam> addEducationLevelSelection(long telegramId,
                                                                EducationLevelParam param,
                                                                List<EducationLevelParam> educationLevelParams) {
        educationLevelParams.add(param);
        return educationLevelParams;
    }

    @Override
    @CachePut(value = "educationSelections", key = "#telegramId")
    public List<EducationLevelParam> removeEducationLevelSelection(long telegramId,
                                                                   EducationLevelParam param,
                                                                   List<EducationLevelParam> educationLevelParams) {
        educationLevelParams.remove(param);
        return educationLevelParams;
    }

    @Override
    @Cacheable(value = "educationSelections")
    public List<EducationLevelParam> getSelectedEducationLevels(long telegramId) {
        return new ArrayList<>(0);
    }

    @Override
    @CacheEvict(value = "educationSelections")
    public void clearEducationLevelSelections(long telegramId) {
        // clearing cache, doesn't need implementing
    }

    @Override
    @CachePut(value = "scheduleSelections", key = "#telegramId")
    public List<ScheduleTypeParam> addScheduleTypeSelection(long telegramId,
                                                            ScheduleTypeParam param,
                                                            List<ScheduleTypeParam> scheduleTypeParams) {
        scheduleTypeParams.add(param);
        return scheduleTypeParams;
    }

    @Override
    @CachePut(value = "scheduleSelections", key = "#telegramId")
    public List<ScheduleTypeParam> removeScheduleTypeSelection(long telegramId,
                                                               ScheduleTypeParam param,
                                                               List<ScheduleTypeParam> scheduleTypeParams) {
        scheduleTypeParams.remove(param);
        return scheduleTypeParams;
    }

    @Override
    @Cacheable(value = "scheduleSelections")
    public List<ScheduleTypeParam> getSelectedScheduleTypes(long telegramId) {
        return new ArrayList<>(0);
    }

    @Override
    @CacheEvict(value = "scheduleSelections")
    public void clearScheduleTypeSelections(long telegramId) {
        // clearing cache, doesn't need implementing
    }
}
