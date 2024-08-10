package com.bipbup.handlers.impl;

import com.bipbup.dao.AppUserConfigDAO;
import com.bipbup.dao.AppUserDAO;
import com.bipbup.entity.AppUser;
import com.bipbup.enums.AppUserState;
import com.bipbup.handlers.StateHandler;
import com.bipbup.utils.Decoder;
import com.bipbup.utils.ConfigUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hashids.Hashids;
import org.springframework.stereotype.Component;

import static com.bipbup.enums.AppUserState.*;

@Slf4j
@RequiredArgsConstructor
@Component
public class QueryUpdateStateHandler implements StateHandler {
    protected static final String COMMAND_CANCEL = "/cancel";
    protected static final String PREFIX_EDIT_CONFIG_NAME = "edit_config_name_";
    protected static final String PREFIX_EDIT_QUERY = "edit_query_";
    protected static final String PREFIX_EDIT_AREA = "edit_area_";
    protected static final String PREFIX_EDIT_EXPERIENCE = "edit_experience_";
    protected static final String PREFIX_EDIT_EDUCATION = "edit_education_";
    protected static final String PREFIX_EDIT_SCHEDULE = "edit_schedule_";
    protected static final String MESSAGE_COMMAND_CANCELLED = "Команда отменена!";
    protected static final String MESSAGE_CONFIGURATION_NOT_FOUND = "Конфигурация не найдена";
    protected static final String MESSAGE_ERROR_PROCESSING_COMMAND = "Ошибка при обработке команды. Попробуйте еще раз.";
    protected static final String MESSAGE_UNEXPECTED_ERROR = "Произошла ошибка. Попробуйте еще раз.";
    protected static final String ENTER_CONFIG_NAME_MESSAGE = "Введите новое название конфигурации:";
    protected static final String ENTER_QUERY_MESSAGE = "Введите новый запрос:";
    protected static final String ENTER_AREA_MESSAGE = "Введите название региона:";
    protected static final String SELECT_EXPERIENCE_MESSAGE = "Выберите опыт работы:";
    protected static final String SELECT_EDUCATION_MESSAGE = "Выберите уровень образования:";
    protected static final String SELECT_SCHEDULE_MESSAGE = "Выберите график работы:";

    private final AppUserDAO appUserDAO;
    private final AppUserConfigDAO appUserConfigDAO;
    private final Decoder decoder;
    private final ConfigUtil configUtil;

    @Override
    public String process(AppUser appUser, String text) {
        try {
            if (COMMAND_CANCEL.equals(text)) {
                return cancelCommand(appUser);
            } else if (text.startsWith(PREFIX_EDIT_CONFIG_NAME)) {
                return handleEditConfigCommand(appUser,
                        text,
                        PREFIX_EDIT_CONFIG_NAME.length(),
                        WAIT_CONFIG_NAME_STATE,
                        ENTER_CONFIG_NAME_MESSAGE);
            } else if (text.startsWith(PREFIX_EDIT_QUERY)) {
                return handleEditConfigCommand(appUser,
                        text,
                        PREFIX_EDIT_QUERY.length(),
                        WAIT_QUERY_STATE,
                        ENTER_QUERY_MESSAGE);
            }
        } catch (NumberFormatException e) {
            log.error("Failed to parse configId from text: {}", text, e);
            return MESSAGE_ERROR_PROCESSING_COMMAND;
        } catch (Exception e) {
            log.error("An unexpected error occurred while processing text: {}", text, e);
            return MESSAGE_UNEXPECTED_ERROR;
        }
        return "";
    }

    private String cancelCommand(AppUser appUser) {
        appUser.setState(BASIC_STATE);
        appUserDAO.saveAndFlush(appUser);
        log.debug("User {} cancelled the command and state set to BASIC_STATE", appUser.getFirstName());
        return MESSAGE_COMMAND_CANCELLED;
    }

    private String handleEditConfigCommand(AppUser appUser,
                                           String text,
                                           int prefixLength,
                                           AppUserState state,
                                           String message) {
        var hash = text.substring(prefixLength);
        var configId = decoder.decode(hash);
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
