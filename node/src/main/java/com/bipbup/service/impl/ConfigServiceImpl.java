package com.bipbup.service.impl;

import com.bipbup.dao.AppUserConfigDAO;
import com.bipbup.dao.EducationLevelDao;
import com.bipbup.dao.ScheduleParamEntityDAO;
import com.bipbup.entity.AppUser;
import com.bipbup.entity.AppUserConfig;
import com.bipbup.service.ConfigService;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ConfigServiceImpl implements ConfigService {

    private final AppUserConfigDAO appUserConfigDAO;

    private final EducationLevelDao educationLevelDao;

    private final ScheduleParamEntityDAO scheduleParamEntityDAO;

    @Override
    public List<AppUserConfig> getConfigsFromPage(int numOfPage, int sizeOfPage) {
        var pageRequest = PageRequest.of(numOfPage, sizeOfPage);
        var pageResult = appUserConfigDAO.findAll(pageRequest);
        return pageResult.toList();
    }

    // было saveAndFlush()
    @Override
    @Transactional
    public AppUserConfig saveConfig(AppUserConfig config, boolean updateParams) {
        if (updateParams) {
            scheduleParamEntityDAO.deleteAllByConfig(config);
            educationLevelDao.deleteAllByConfig(config);
            educationLevelDao.saveAll(config.getEducationLevels());
            scheduleParamEntityDAO.saveAll(config.getScheduleTypes());
        }

        return appUserConfigDAO.save(config);
    }

    @Override
    @Transactional
    public void deleteConfig(AppUserConfig config) {
        scheduleParamEntityDAO.deleteAllByConfig(config);
        educationLevelDao.deleteAllByConfig(config);
        appUserConfigDAO.delete(config);
    }

    @Override
    public Optional<AppUserConfig> getConfigById(long id) {
        return appUserConfigDAO.findById(id);
    }

    @Override
    public List<AppUserConfig> getConfigByUser(AppUser user) {
        return appUserConfigDAO.findByAppUser(user);
    }

    @Override
    public Long countOfConfigs(AppUser user) {
        return appUserConfigDAO.countAppUserConfigByAppUser(user);
    }
}
