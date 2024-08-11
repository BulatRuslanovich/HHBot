package com.bipbup.handlers.impl;

import com.bipbup.dao.AppUserConfigDAO;
import com.bipbup.dao.AppUserDAO;
import com.bipbup.entity.AppUser;
import com.bipbup.entity.AppUserConfig;
import com.bipbup.enums.EnumParam;
import com.bipbup.handlers.Cancellable;
import com.bipbup.handlers.StateHandler;
import com.bipbup.utils.Decoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.bipbup.enums.AppUserState.QUERY_DELETE_STATE;
import static com.bipbup.enums.AppUserState.QUERY_UPDATE_STATE;

@Slf4j
@Component
public class QueryMenuStateHandler extends Cancellable implements StateHandler {

    private final AppUserConfigDAO appUserConfigDAO;

    private final Decoder decoder;

    protected static final String DELETE_PREFIX = "delete_";
    protected static final String UPDATE_PREFIX = "update_";
    protected static final String MESSAGE_DELETE_CONFIRMATION = "Вы уверены, что хотите удалить этот запрос?";
    protected static final String MESSAGE_CONFIGURATION_NOT_FOUND = "Конфигурация не найдена.";

    public QueryMenuStateHandler(AppUserDAO appUserDAO,
                                 BasicStateHandler basicStateHandler,
                                 AppUserConfigDAO appUserConfigDAO,
                                 Decoder decoder) {
        super(appUserDAO, basicStateHandler);
        this.appUserConfigDAO = appUserConfigDAO;
        this.decoder = decoder;
    }

    @Override
    public String process(AppUser user, String input) {
        if (isCancelCommand(input)) return processCancelCommand(user);
        if (isBasicCommand(input)) return processBasicCommand(user, input);
        if (hasDeletePrefix(input)) return processDeleteCommand(user);
        if (hasUpdatePrefix(input)) return processUpdateCommand(user, input);
        
        return "";
    }
    
    private boolean hasUpdatePrefix(String input) {
        return input.startsWith(UPDATE_PREFIX);
    }

    private boolean hasDeletePrefix(String input) {
        return input.startsWith(DELETE_PREFIX);
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

    private String showDetailedQueryOutput(AppUserConfig config) {
        StringBuilder output = new StringBuilder()
                .append(config.getConfigName())
                .append("\nТекст запроса: ").append(config.getQueryText())
                .append("\nРегион: ").append(config.getRegion() == null ? "Любой" : config.getRegion())
                .append("\nОпыт работы: ").append(config.getExperience().getDescription());

        appendEnumParams(output, config.getEducationLevels(), "\nУровень образования: ");
        appendEnumParams(output, config.getScheduleTypes(), "\nТип графика: ");

        return output.toString();
    }

    private String processDeleteCommand(AppUser user) {
        user.setState(QUERY_DELETE_STATE);
        appUserDAO.saveAndFlush(user);
        log.debug("User {} set state to QUERY_DELETE_STATE", user.getFirstName());
        return MESSAGE_DELETE_CONFIRMATION;
    }

    private String processUpdateCommand(AppUser user, String input) {
        var hash = input.substring(UPDATE_PREFIX.length());
        var configId = decoder.idOf(hash);
        var optionalAppUserConfig = appUserConfigDAO.findById(configId);

        if (optionalAppUserConfig.isPresent()) {
            AppUserConfig config = optionalAppUserConfig.get();

            user.setState(QUERY_UPDATE_STATE);
            appUserDAO.saveAndFlush(user);
            log.debug("User {} set state to QUERY_UPDATE_STATE", user.getFirstName());

            return showDetailedQueryOutput(config);
        } else {
            log.warn("Configuration with id {} not found for user {}", configId, user.getFirstName());
            return MESSAGE_CONFIGURATION_NOT_FOUND;
        }
    }
}
