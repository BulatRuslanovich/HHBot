package com.bipbup.service.impl;

import com.bipbup.dao.AppUserConfigDAO;
import com.bipbup.entity.AppUser;
import com.bipbup.entity.AppUserConfig;
import com.bipbup.service.ConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
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
    public Optional<AppUserConfig> getById(Long configId) {
        return appUserConfigDAO.findById(configId);
    }

    @Override
    public List<AppUserConfig> getByUser(final AppUser user) {
        return appUserConfigDAO.findByAppUser(user);
    }

    @Override
    @CachePut(value = "configSelections", key = "#telegramId")
    public Long saveConfigSelection(Long telegramId, Long configId) {
        return configId;
    }

    @Override
    @Cacheable(value = "configSelections")
    public Long getSelectedConfigId(Long telegramId) {
        return null;
    }

    @Override
    @CacheEvict(value = "configSelections")
    public void clearConfigSelection(Long telegramId) {
        // clearing cache, doesn't need implementing
    }
}
