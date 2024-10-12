package com.bipbup.dao;

import com.bipbup.entity.AppUserConfig;
import com.bipbup.entity.ScheduleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScheduleParamEntityDAO extends JpaRepository<ScheduleType, Long> {

    void deleteAllByConfig(AppUserConfig config);
}
