package com.bipbup.handlers.impl;

import com.bipbup.dao.AppUserConfigDAO;
import com.bipbup.dao.AppUserDAO;
import com.bipbup.entity.AppUser;
import com.bipbup.handlers.Cancellable;
import com.bipbup.handlers.StateHandler;
import com.bipbup.utils.Decoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.bipbup.enums.AppUserState.BASIC_STATE;

@Slf4j
@Component
public class QueryDeleteStateHandler extends Cancellable implements StateHandler {

    private final AppUserConfigDAO appUserConfigDAO;

    private final Decoder decoder;

    protected static final String DELETE_YES_PREFIX = "delete_yes_";
    protected static final String DELETE_NO_COMMAND = "delete_no";
    protected static final String MESSAGE_CONFIGURATION_DELETED = "Конфигурация была удалена.";
    protected static final String MESSAGE_CONFIGURATION_NOT_DELETED = "Конфигурация не была удалена.";
    protected static final String MESSAGE_CONFIGURATION_NOT_FOUND = "Конфигурация не найдена.";
    protected static final String MESSAGE_ERROR_PROCESSING_COMMAND = "Ошибка при обработке команды. Попробуйте еще раз.";
    protected static final String MESSAGE_UNEXPECTED_ERROR = "Произошла ошибка. Попробуйте еще раз.";

    public QueryDeleteStateHandler(AppUserDAO appUserDAO,
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
        if (hasDeleteYesPrefix(input)) return processDeleteYesCommand(user, input);
        if (isDeleteNoCommand(input)) return processDeleteNoCommand(user);

        return "";
    }

    private boolean isDeleteNoCommand(final String input) {
        return DELETE_NO_COMMAND.equals(input);
    }

    private boolean hasDeleteYesPrefix(final String input) {
        return input.startsWith(DELETE_YES_PREFIX);
    }

    private String processDeleteYesCommand(AppUser user, String input) {
        var hash = input.substring(DELETE_YES_PREFIX.length());
        var configId = decoder.idOf(hash);
        var optional = appUserConfigDAO.findById(configId);

        if (optional.isPresent()) {
            appUserConfigDAO.delete(optional.get());
            user.setState(BASIC_STATE);
            appUserDAO.saveAndFlush(user);
            log.debug("User {} deleted configuration with id {} and state set to BASIC_STATE", user.getFirstName(), configId);
            return MESSAGE_CONFIGURATION_DELETED;
        } else {
            log.warn("Configuration with id {} not found for user {}", configId, user.getFirstName());
            return MESSAGE_CONFIGURATION_NOT_FOUND;
        }
    }

    private String processDeleteNoCommand(AppUser user) {
        user.setState(BASIC_STATE);
        appUserDAO.saveAndFlush(user);
        log.debug("User {} chose not to delete the configuration and state set to BASIC_STATE", user.getFirstName());
        return MESSAGE_CONFIGURATION_NOT_DELETED;
    }
}
