package com.bipbup.dao;

import com.bipbup.entity.AppUserConfig;
import com.bipbup.entity.EducationLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EducationLevelDao extends JpaRepository<EducationLevel, Long> {

    void deleteAllByConfig(AppUserConfig config);
}
