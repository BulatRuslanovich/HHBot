package com.bipbup.handlers.impl;

import com.bipbup.entity.AppUser;
import com.bipbup.enums.AppUserState;
import com.bipbup.handlers.StateHandler;
import com.bipbup.service.ConfigService;
import com.bipbup.service.UserService;
import com.bipbup.utils.Decoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.bipbup.enums.AppUserState.WAIT_CONFIG_NAME_STATE;
import static com.bipbup.enums.AppUserState.WAIT_QUERY_STATE;

@Slf4j
@Component
public class QueryUpdateStateHandler implements StateHandler {
    private final UserService userService;

    private final ConfigService configService;

    private final Decoder decoder;

    protected static final String UPDATE_CONFIG_NAME_PREFIX = "update_config_name_";
    protected static final String UPDATE_QUERY_PREFIX = "update_query_";
    protected static final String UPDATE_AREA_PREFIX = "update_area_";
    protected static final String UPDATE_EXPERIENCE_PREFIX = "update_experience_";
    protected static final String UPDATE_EDUCATION_PREFIX = "update_education_";
    protected static final String UPDATE_SCHEDULE_PREFIX = "update_schedule_";
    protected static final String MESSAGE_CONFIGURATION_NOT_FOUND = "Конфигурация не найдена";
    protected static final String MESSAGE_ERROR_PROCESSING_COMMAND = "Ошибка при обработке команды. Попробуйте еще раз.";
    protected static final String MESSAGE_UNEXPECTED_ERROR = "Произошла ошибка. Попробуйте еще раз.";
    protected static final String ENTER_CONFIG_NAME_MESSAGE = "Введите новое название конфигурации:";
    protected static final String ENTER_QUERY_MESSAGE = "Введите новый запрос:";
    protected static final String ENTER_AREA_MESSAGE = "Введите название региона:";
    protected static final String SELECT_EXPERIENCE_MESSAGE = "Выберите опыт работы:";
    protected static final String SELECT_EDUCATION_MESSAGE = "Выберите уровень образования:";
    protected static final String SELECT_SCHEDULE_MESSAGE = "Выберите график работы:";

    public QueryUpdateStateHandler(UserService userService,
                                   Decoder decoder,
                                   ConfigService configService) {
        this.userService = userService;
        this.decoder = decoder;
        this.configService = configService;
    }

    @Override
    public String process(AppUser user, String input) {
        if (hasEditConfigNamePrefix(input))
            return processEditConfigCommand(user, input,
                    UPDATE_CONFIG_NAME_PREFIX.length(),
                    WAIT_CONFIG_NAME_STATE,
                    ENTER_CONFIG_NAME_MESSAGE);
        if (hasEditQueryPrefix(input))
            return processEditConfigCommand(user, input,
                    UPDATE_QUERY_PREFIX.length(),
                    WAIT_QUERY_STATE,
                    ENTER_QUERY_MESSAGE);
        
        return "";
    }
    
    private boolean hasEditQueryPrefix(String input) {
        return input.startsWith(UPDATE_QUERY_PREFIX);
    }

    private boolean hasEditConfigNamePrefix(String input) {
        return input.startsWith(UPDATE_CONFIG_NAME_PREFIX);
    }

    private String processEditConfigCommand(AppUser user,
                                            String input,
                                            int prefixLength,
                                            AppUserState state,
                                            String message) {
        var hash = input.substring(prefixLength);
        var configId = decoder.idOf(hash);
        var optional = configService.getById(configId);

        if (optional.isPresent()) {
            userService.saveUserState(user.getTelegramId(), state);
            configService.saveConfigSelection(user.getTelegramId(), configId);
            log.debug("User {} selected parameter to edit and state set to {}", user.getFirstName(), state);
            return message;
        } else {
            log.warn("Configuration with id {} not found for user {}", configId, user.getFirstName());
            return MESSAGE_CONFIGURATION_NOT_FOUND;
        }
    }
}
