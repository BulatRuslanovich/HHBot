package com.bipbup.handlers.impl;

import com.bipbup.dao.AppUserConfigDAO;
import com.bipbup.dao.AppUserDAO;
import com.bipbup.entity.AppUser;
import com.bipbup.entity.AppUserConfig;
import com.bipbup.enums.EnumParam;
import com.bipbup.handlers.StateHandler;
import com.bipbup.utils.Decoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.bipbup.enums.AppUserState.BASIC_STATE;
import static com.bipbup.enums.AppUserState.QUERY_DELETE_STATE;
import static com.bipbup.enums.AppUserState.QUERY_UPDATE_STATE;

@Slf4j
@RequiredArgsConstructor
@Component
public class QueryMenuStateHandler implements StateHandler {
    protected static final String COMMAND_CANCEL = "/cancel";
    protected static final String COMMAND_BACK_TO_QUERY_LIST = "back_to_query_list";
    protected static final String COMMAND_MY_QUERIES = "/myqueries";
    protected static final String COMMAND_NEW_QUERY = "/newquery";
    protected static final String PREFIX_DELETE = "delete_";
    protected static final String PREFIX_UPDATE = "update_";
    protected static final String MESSAGE_COMMAND_CANCELLED = "Команда отменена!";
    protected static final String MESSAGE_DELETE_CONFIRMATION = "Вы уверены, что хотите удалить этот запрос?";
    protected static final String MESSAGE_CONFIGURATION_NOT_FOUND = "Конфигурация не найдена.";

    private final BasicStateHandler basicStateHandler;

    private final AppUserDAO appUserDAO;

    private final AppUserConfigDAO appUserConfigDAO;

    private final Decoder decoder;

    @Override
    public String process(AppUser appUser, String text) {
        return switch (text) {
            case COMMAND_CANCEL -> handleCancelCommand(appUser);
            case COMMAND_BACK_TO_QUERY_LIST, COMMAND_MY_QUERIES -> handleMyQueriesCommand(appUser);
            case COMMAND_NEW_QUERY -> handleNewQueryCommand(appUser);
            default -> {
                if (text.startsWith(PREFIX_DELETE)) {
                    yield handleDeleteCommand(appUser);
                } else if (text.startsWith(PREFIX_UPDATE)) {
                    yield handleUpdateCommand(appUser, text);
                }
                yield "";
            }
        };
    }

    private String handleCancelCommand(AppUser appUser) {
        appUser.setState(BASIC_STATE);
        appUserDAO.saveAndFlush(appUser);
        log.debug("User {} cancelled the command and state set to BASIC_STATE", appUser.getFirstName());
        return MESSAGE_COMMAND_CANCELLED;
    }

    private String handleMyQueriesCommand(AppUser appUser) {
        return basicStateHandler.process(appUser, COMMAND_MY_QUERIES);
    }

    private String handleNewQueryCommand(AppUser appUser) {
        return basicStateHandler.process(appUser, COMMAND_NEW_QUERY);
    }

    private String handleDeleteCommand(AppUser appUser) {
        appUser.setState(QUERY_DELETE_STATE);
        appUserDAO.saveAndFlush(appUser);
        log.debug("User {} set state to QUERY_DELETE_STATE", appUser.getFirstName());
        return MESSAGE_DELETE_CONFIRMATION;
    }

    private String handleUpdateCommand(AppUser appUser, String text) {
        var hash = text.substring(PREFIX_UPDATE.length());
        var configId = decoder.idOf(hash);
        var optionalAppUserConfig = appUserConfigDAO.findById(configId);

        if (optionalAppUserConfig.isPresent()) {
            AppUserConfig config = optionalAppUserConfig.get();

            appUser.setState(QUERY_UPDATE_STATE);
            appUserDAO.saveAndFlush(appUser);
            log.debug("User {} set state to QUERY_UPDATE_STATE", appUser.getFirstName());

            return showDetailedQueryOutput(config);
        } else {
            log.warn("Configuration with id {} not found for user {}", configId, appUser.getFirstName());
            return MESSAGE_CONFIGURATION_NOT_FOUND;
        }
    }

    private String showDetailedQueryOutput(AppUserConfig config) {
        StringBuilder output = new StringBuilder()
                .append(config.getConfigName())
                .append("\nТекст запроса: ").append(config.getQueryText())
                .append("\nРегион: ").append(config.getRegion())
                .append("\nОпыт работы: ").append(config.getExperience().getDescription());

        appendEnumParams(output, config.getEducationLevels(), "\nУровень образования: ");
        appendEnumParams(output, config.getScheduleTypes(), "\nТип графика: ");

        return output.toString();
    }

    private void appendEnumParams(StringBuilder output, EnumParam[] values, String prefix) {
        if (values != null && values.length > 0) {
            output.append(prefix);
            for (EnumParam value : values) {
                output.append(value.getDescription()).append(", ");
            }
            output.setLength(output.length() - " ,".length());
        }
    }
}
