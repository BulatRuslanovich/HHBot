package com.bipbup.service.cache;

import com.bipbup.enums.impl.ScheduleTypeParam;
import java.util.List;


public interface ScheduleTypeCacheService {

    @SuppressWarnings("UnusedReturnValue")
    List<ScheduleTypeParam> putScheduleTypes(long telegramId, ScheduleTypeParam param, List<ScheduleTypeParam> params);

    @SuppressWarnings("UnusedReturnValue")
    List<ScheduleTypeParam> removeScheduleTypes(long telegramId, ScheduleTypeParam param,
                                                List<ScheduleTypeParam> params);

    List<ScheduleTypeParam> getScheduleTypes(long telegramId);

    void clearScheduleTypes(long telegramId);
}
