package com.bipbup.dao;

import com.bipbup.entity.AppUserConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppUserConfigDAO extends JpaRepository<AppUserConfig, Long> {
}
