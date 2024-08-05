package com.bipbup.handlers.impl;

import com.bipbup.dao.AppUserConfigDAO;
import com.bipbup.entity.AppUser;
import com.bipbup.entity.AppUserConfig;
import com.bipbup.enums.EnumParam;
import com.bipbup.handlers.StateHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class QuerySelectionStateHandler implements StateHandler {
    private final BasicStateHandler basicStateHandler;
    private final AppUserConfigDAO appUserConfigDAO;

    @Override
    public String process(final AppUser appUser, final String callbackData) {
        String prefix = "query_";
        if (callbackData.startsWith(prefix)) {
            long queryId;

            try {
                queryId = Long.parseLong(callbackData
                        .substring(prefix.length()));
            } catch (NumberFormatException e) {
                return "";
            }

            return showQueryOutput(queryId);
        } else if (callbackData.equals("/newquery")) {
            return basicStateHandler.process(appUser, callbackData);
        } else if (callbackData.equals("back_to_query_list")) {
            return basicStateHandler.process(appUser, "/myqueries");
        } else {
            return "";
        }
    }

    private String showQueryOutput(final long configId) {
        var optionalAppUserConfig = appUserConfigDAO.findById(configId);

        if (optionalAppUserConfig.isEmpty()) {
            return "Конфигурация не найдена";
        }

        AppUserConfig config = optionalAppUserConfig.get();

        StringBuilder stringBuilder = new StringBuilder()
                .append(config.getConfigName())
                .append("\nТекст запроса: ")
                .append(config.getQueryText())
                .append("\nРегион: ")
                .append(config.getRegion())
                .append("\nОпыт работы: ")
                .append(config.getExperience().getDescription());

        appendEnumParams(stringBuilder,
                config.getEducationLevels(),
                "\nУровень образования: ");
        appendEnumParams(stringBuilder,
                config.getScheduleTypes(),
                "\nТип графика: ");

        return stringBuilder.toString();
    }

    private void appendEnumParams(final StringBuilder stringBuilder,
                                  final EnumParam[] values,
                                  final String prefix) {
        if (values != null && values.length != 0) {
            stringBuilder.append(prefix);

            for (var value : values) {
                stringBuilder.append(value.getDescription()).append(", ");
            }

            stringBuilder.delete(stringBuilder.length() - ", ".length(),
                    stringBuilder.length());
        }
    }
}
