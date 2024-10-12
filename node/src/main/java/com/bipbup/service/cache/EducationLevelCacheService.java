package com.bipbup.service.cache;

import com.bipbup.enums.impl.EducationLevelParam;
import java.util.List;

public interface EducationLevelCacheService {

    @SuppressWarnings("UnusedReturnValue")
    List<EducationLevelParam> putEducationLevels(long telegramId, EducationLevelParam param,
                                                         List<EducationLevelParam> educationLevelParams);

    @SuppressWarnings("UnusedReturnValue")
    List<EducationLevelParam> removeEducationLevels(long telegramId, EducationLevelParam param,
                                                   List<EducationLevelParam> educationLevelParams);

    List<EducationLevelParam> getEducationLevels(long telegramId);

    void clearEducationLevels(long telegramId);
}
