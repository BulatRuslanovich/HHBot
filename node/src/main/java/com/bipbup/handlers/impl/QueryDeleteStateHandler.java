package com.bipbup.handlers.impl;

import com.bipbup.dao.AppUserConfigDAO;
import com.bipbup.dao.AppUserDAO;
import com.bipbup.entity.AppUser;
import com.bipbup.handlers.StateHandler;
import com.bipbup.utils.Decoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.bipbup.enums.AppUserState.BASIC_STATE;

@Slf4j
@RequiredArgsConstructor
@Component
public class QueryDeleteStateHandler implements StateHandler {
    protected static final String COMMAND_CANCEL = "/cancel";
    protected static final String PREFIX_DELETE_YES = "delete_yes_";
    protected static final String COMMAND_DELETE_NO = "delete_no";
    protected static final String MESSAGE_COMMAND_CANCELLED = "Команда отменена!";
    protected static final String MESSAGE_CONFIGURATION_DELETED = "Конфигурация была удалена.";
    protected static final String MESSAGE_CONFIGURATION_NOT_DELETED = "Конфигурация не была удалена.";
    protected static final String MESSAGE_CONFIGURATION_NOT_FOUND = "Конфигурация не найдена.";
    protected static final String MESSAGE_ERROR_PROCESSING_COMMAND = "Ошибка при обработке команды. Попробуйте еще раз.";
    protected static final String MESSAGE_UNEXPECTED_ERROR = "Произошла ошибка. Попробуйте еще раз.";

    private final AppUserDAO appUserDAO;

    private final AppUserConfigDAO appUserConfigDAO;

    private final Decoder decoder;

    @Override
    public String process(AppUser appUser, String text) {
        try {
            if (COMMAND_CANCEL.equals(text)) {
                return cancelCommand(appUser);
            } else if (text.startsWith(PREFIX_DELETE_YES)) {
                return handleDeleteYesCommand(appUser, text);
            } else if (COMMAND_DELETE_NO.equals(text)) {
                return handleDeleteNoCommand(appUser);
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

    private String handleDeleteYesCommand(AppUser appUser, String text) {
        var hash = text.substring(PREFIX_DELETE_YES.length());
        var configId = decoder.idOf(hash);
        var optional = appUserConfigDAO.findById(configId);

        if (optional.isPresent()) {
            appUserConfigDAO.delete(optional.get());
            appUser.setState(BASIC_STATE);
            appUserDAO.saveAndFlush(appUser);
            log.debug("User {} deleted configuration with id {} and state set to BASIC_STATE", appUser.getFirstName(), configId);
            return MESSAGE_CONFIGURATION_DELETED;
        } else {
            log.warn("Configuration with id {} not found for user {}", configId, appUser.getFirstName());
            return MESSAGE_CONFIGURATION_NOT_FOUND;
        }
    }

    private String handleDeleteNoCommand(AppUser appUser) {
        appUser.setState(BASIC_STATE);
        appUserDAO.saveAndFlush(appUser);
        log.debug("User {} chose not to delete the configuration and state set to BASIC_STATE", appUser.getFirstName());
        return MESSAGE_CONFIGURATION_NOT_DELETED;
    }
}
