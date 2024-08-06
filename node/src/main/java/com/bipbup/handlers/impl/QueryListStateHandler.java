package com.bipbup.handlers.impl;

import com.bipbup.entity.AppUser;
import com.bipbup.entity.AppUserConfig;
import com.bipbup.enums.EnumParam;
import com.bipbup.handlers.StateHandler;
import com.bipbup.utils.UserConfigUtil;
import com.bipbup.utils.UserUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.bipbup.enums.AppUserState.BASIC_STATE;
import static com.bipbup.enums.AppUserState.QUERY_MENU_STATE;

@RequiredArgsConstructor
@Component
public class QueryListStateHandler implements StateHandler {
    private static final String QUERY_OUTPUT_FORMAT = """
            Конфигурация "%s" с запросом "%s"
            Что хотите сделать с ней?""";

    private final UserUtil userUtil;
    private final UserConfigUtil userConfigUtil;
    private final BasicStateHandler basicStateHandler;

    @Override
    public String process(AppUser appUser, String text) {
        if (text.equals("/cancel")) {
            userUtil.updateUserState(appUser, BASIC_STATE);
            return "Команда отменена!";
        }

        String prefix = "query_";

        if (text.startsWith(prefix)) {
            long queryId;

            try {
                queryId = Long.parseLong(text.substring(prefix.length()));
            } catch (NumberFormatException e) {
                return "";
            }

            userUtil.updateUserState(appUser, QUERY_MENU_STATE);
            return newShowQueryOutput(queryId);
        }

        if (text.equals("/myqueries")) {
            return basicStateHandler.process(appUser, "/myqueries");
        }

        if (text.equals("/newquery")) {
            return basicStateHandler.process(appUser, "/newquery");
        }

        return "";
    }

    private String newShowQueryOutput(final long configId) {
        var optionalAppUserConfig = userConfigUtil.getConfigById(configId);

        if (optionalAppUserConfig.isEmpty()) {
            return "Конфигурация не найдена";
        }

        AppUserConfig config = optionalAppUserConfig.get();

        return String.format(QUERY_OUTPUT_FORMAT,
                config.getConfigName(),
                config.getQueryText());
    }

    private String showQueryOutput(final long configId) {
        var optionalAppUserConfig = userConfigUtil.getConfigById(configId);

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
