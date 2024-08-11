package com.bipbup.handlers.impl;

import com.bipbup.dao.AppUserConfigDAO;
import com.bipbup.dao.AppUserDAO;
import com.bipbup.entity.AppUser;
import com.bipbup.enums.AppUserState;
import com.bipbup.handlers.Cancellable;
import com.bipbup.handlers.StateHandler;
import com.bipbup.utils.ConfigUtil;
import com.bipbup.utils.Decoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.bipbup.enums.AppUserState.*;

@Slf4j
@Component
public class QueryUpdateStateHandler extends Cancellable implements StateHandler {

    private final AppUserConfigDAO appUserConfigDAO;

    private final Decoder decoder;

    private final ConfigUtil configUtil;

    protected static final String EDIT_CONFIG_NAME_PREFIX = "edit_config_name_";
    protected static final String EDIT_QUERY_PREFIX = "edit_query_";
    protected static final String EDIT_AREA_PREFIX = "edit_area_";
    protected static final String EDIT_EXPERIENCE_PREFIX = "edit_experience_";
    protected static final String EDIT_EDUCATION_PREFIX = "edit_education_";
    protected static final String EDIT_SCHEDULE_PREFIX = "edit_schedule_";
    protected static final String MESSAGE_CONFIGURATION_NOT_FOUND = "Конфигурация не найдена";
    protected static final String MESSAGE_ERROR_PROCESSING_COMMAND = "Ошибка при обработке команды. Попробуйте еще раз.";
    protected static final String MESSAGE_UNEXPECTED_ERROR = "Произошла ошибка. Попробуйте еще раз.";
    protected static final String ENTER_CONFIG_NAME_MESSAGE = "Введите новое название конфигурации:";
    protected static final String ENTER_QUERY_MESSAGE = "Введите новый запрос:";
    protected static final String ENTER_AREA_MESSAGE = "Введите название региона:";
    protected static final String SELECT_EXPERIENCE_MESSAGE = "Выберите опыт работы:";
    protected static final String SELECT_EDUCATION_MESSAGE = "Выберите уровень образования:";
    protected static final String SELECT_SCHEDULE_MESSAGE = "Выберите график работы:";

    public QueryUpdateStateHandler(AppUserDAO appUserDAO,
                                   BasicStateHandler basicStateHandler,
                                   AppUserConfigDAO appUserConfigDAO,
                                   Decoder decoder,
                                   ConfigUtil configUtil) {
        super(appUserDAO, basicStateHandler);
        this.appUserConfigDAO = appUserConfigDAO;
        this.decoder = decoder;
        this.configUtil = configUtil;
    }

    @Override
    public String process(AppUser user, String input) {
        if (isCancelCommand(input)) return processCancelCommand(user);
        if (isBasicCommand(input)) return processBasicCommand(user, input);
        if (hasEditConfigNamePrefix(input)) {
            return processEditConfigCommand(user, input,
                    EDIT_CONFIG_NAME_PREFIX.length(),
                    WAIT_CONFIG_NAME_STATE,
                    ENTER_CONFIG_NAME_MESSAGE);
        }
        
        if (hasEditQueryPrefix(input)) {
            return processEditConfigCommand(user, input,
                    EDIT_QUERY_PREFIX.length(),
                    WAIT_QUERY_STATE,
                    ENTER_QUERY_MESSAGE);
        }
        
        return "";
    }
    
    private boolean hasEditQueryPrefix(String input) {
        return input.startsWith(EDIT_QUERY_PREFIX);
    }

    private boolean hasEditConfigNamePrefix(String input) {
        return input.startsWith(EDIT_CONFIG_NAME_PREFIX);
    }

    private String processEditConfigCommand(AppUser appUser,
                                            String input,
                                            int prefixLength,
                                            AppUserState state,
                                            String message) {
        var hash = input.substring(prefixLength);
        var configId = decoder.idOf(hash);
        var optional = appUserConfigDAO.findById(configId);

        if (optional.isPresent()) {
            appUser.setState(state);
            appUserDAO.saveAndFlush(appUser);
            configUtil.saveConfigSelection(appUser.getTelegramId(), configId);
            log.debug("User {} selected parameter to edit and state set to {}", appUser.getFirstName(), state);
            return message;
        } else {
            log.warn("Configuration with id {} not found for user {}", configId, appUser.getFirstName());
            return MESSAGE_CONFIGURATION_NOT_FOUND;
        }
    }
}
