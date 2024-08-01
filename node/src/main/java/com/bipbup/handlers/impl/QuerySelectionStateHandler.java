package com.bipbup.handlers.impl;

import com.bipbup.entity.AppUser;
import com.bipbup.entity.AppUserConfig;
import com.bipbup.enums.EducationLevelParam;
import com.bipbup.enums.ExperienceParam;
import com.bipbup.enums.ScheduleTypeParam;
import com.bipbup.handlers.StateHandler;
import com.bipbup.utils.UserUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@RequiredArgsConstructor
@Component
public class QuerySelectionStateHandler implements StateHandler {
    private final BasicStateHandler basicStateHandler;
    private final UserUtil userUtil;

    @Override
    public String process(AppUser appUser, String callbackData) {
        String prefix = "query_";
        if (callbackData.startsWith(prefix)) {
            long queryId = Long.parseLong(callbackData.substring(prefix.length()));
            return showQueryOutput(queryId);
        } else if (callbackData.equals("/newquery")) {
            return basicStateHandler.process(appUser, callbackData);
        } else {
            return "";
        }
    }

    private String showQueryOutput(long queryId) {
        AppUserConfig appUserConfig = userUtil.getAppUserConfigById(queryId);

        Map<ExperienceParam, String> experienceMap = Map.of(
                ExperienceParam.NO_MATTER, "Не имеет значения",
                ExperienceParam.NO_EXPERIENCE, "Нет опыта",
                ExperienceParam.BETWEEN_1_AND_3, "1-3 года",
                ExperienceParam.BETWEEN_3_AND_6, "3-6 лет",
                ExperienceParam.MORE_THEN_6, "Более 6 лет"
        );
        Map<EducationLevelParam, String> educationLevelMap = Map.of(
                EducationLevelParam.NOT_REQUIRED_OR_NOT_SPECIFIED, "Не требуется или не указано",
                EducationLevelParam.HIGHER, "Высшее",
                EducationLevelParam.SECONDARY_VOCATIONAL, "Среднее специальное"
        );
        Map<ScheduleTypeParam, String> scheduleTypeMap = Map.of(
                ScheduleTypeParam.FULL_DAY, "Полный день",
                ScheduleTypeParam.REMOTE_WORKING, "Удалённая работа",
                ScheduleTypeParam.FLEXIBLE_SCHEDULE, "Гибкий график",
                ScheduleTypeParam.SHIFT_SCHEDULE, "Сменный график"
        );

        StringBuilder stringBuilder = new StringBuilder(appUserConfig.getConfigName());
        stringBuilder.append("\nТекст запроса: ").append(appUserConfig.getQueryText());
        stringBuilder.append("\nРегион: ").append(appUserConfig.getRegion());
        stringBuilder.append("\nОпыт работы: ").append(experienceMap.get(appUserConfig.getExperience()));

        var educationLevels = appUserConfig.getEducationLevels();
        if (educationLevels != null && educationLevels.length != 0) {
            stringBuilder.append("\nУровень образования: ");
            for (var educationLevel : educationLevels) {
                stringBuilder.append(educationLevelMap.get(educationLevel)).append(", ");
            }
            stringBuilder.delete(stringBuilder.length() - 2, stringBuilder.length());
        }

        var scheduleTypes = appUserConfig.getScheduleTypes();
        if (scheduleTypes != null && scheduleTypes.length != 0) {
            stringBuilder.append("\nТип графика: ");
            for (var scheduleType : scheduleTypes) {
                stringBuilder.append(scheduleTypeMap.get(scheduleType)).append(", ");
            }
            stringBuilder.delete(stringBuilder.length() - 2, stringBuilder.length());
        }

        return stringBuilder.toString();
    }
}