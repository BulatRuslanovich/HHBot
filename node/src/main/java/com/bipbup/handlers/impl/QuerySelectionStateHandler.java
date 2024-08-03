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
        } else if (callbackData.equals("back_to_query_list")) {
            return basicStateHandler.process(appUser, "/myqueries");
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

        StringBuilder stringBuilder = new StringBuilder()
                .append(appUserConfig.getConfigName())
                .append("\nТекст запроса: ")
                .append(appUserConfig.getQueryText())
                .append("\nРегион: ")
                .append(appUserConfig.getRegion())
                .append("\nОпыт работы: ")
                .append(experienceMap.get(appUserConfig.getExperience()));

        appendValues(stringBuilder, educationLevelMap, appUserConfig.getEducationLevels(), "\nУровень образования: ");
        appendValues(stringBuilder, scheduleTypeMap, appUserConfig.getScheduleTypes(), "\nТип графика: ");

        return stringBuilder.toString();
    }

    private <T> void appendValues(StringBuilder stringBuilder, Map<T, String> map, T[] values, String prefix) {
        if (values != null && values.length != 0) {
            stringBuilder.append(prefix);
            for (T value : values) {
                stringBuilder.append(map.get(value)).append(", ");
            }
            stringBuilder.delete(stringBuilder.length() - ", ".length(), stringBuilder.length());
        }
    }
}