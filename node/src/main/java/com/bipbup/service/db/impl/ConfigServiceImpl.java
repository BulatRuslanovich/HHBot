package com.bipbup.service.db.impl;

import com.bipbup.dao.AppUserConfigDAO;
import com.bipbup.entity.AppUser;
import com.bipbup.entity.AppUserConfig;
import com.bipbup.service.db.ConfigService;
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

	@Override
	public List<AppUserConfig> getConfigsFromPage(int numOfPage, int sizeOfPage) {
		var pageRequest = PageRequest.of(numOfPage, sizeOfPage);
		var pageResult = appUserConfigDAO.findAll(pageRequest);
		return pageResult.toList();
	}

	@Override
	public AppUserConfig saveConfig(AppUserConfig config) {
		return appUserConfigDAO.save(config);
	}

	@Override
	@Transactional
	public void deleteConfig(AppUserConfig config) {
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
