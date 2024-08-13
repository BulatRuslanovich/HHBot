package com.bipbup.handlers.impl;

import com.bipbup.entity.AppUser;
import com.bipbup.handlers.StateHandler;
import com.bipbup.service.ConfigService;
import com.bipbup.service.UserService;
import com.bipbup.utils.Decoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class QueryDeleteStateHandler implements StateHandler {
    private final UserService userService;

    private final ConfigService configService;

    private final Decoder decoder;

    protected static final String DELETE_CONFIRM_PREFIX = "delete_confirm_";
    protected static final String DELETE_CANCEL_COMMAND = "delete_cancel";
    protected static final String MESSAGE_CONFIGURATION_DELETED = "Конфигурация была удалена.";
    protected static final String MESSAGE_CONFIGURATION_NOT_DELETED = "Конфигурация не была удалена.";
    protected static final String MESSAGE_CONFIGURATION_NOT_FOUND = "Конфигурация не найдена.";
    protected static final String MESSAGE_ERROR_PROCESSING_COMMAND = "Ошибка при обработке команды. Попробуйте еще раз.";
    protected static final String MESSAGE_UNEXPECTED_ERROR = "Произошла ошибка. Попробуйте еще раз.";

    public QueryDeleteStateHandler(UserService userService,
                                   ConfigService configService,
                                   Decoder decoder) {
        this.userService = userService;
        this.configService = configService;
        this.decoder = decoder;
    }

    @Override
    public String process(AppUser user, String input) {
        if (hasDeleteYesPrefix(input)) return processDeleteYesCommand(user, input);
        if (isDeleteNoCommand(input)) return processDeleteNoCommand(user);

        return "";
    }

    private boolean isDeleteNoCommand(final String input) {
        return DELETE_CANCEL_COMMAND.equals(input);
    }

    private boolean hasDeleteYesPrefix(final String input) {
        return input.startsWith(DELETE_CONFIRM_PREFIX);
    }

    private String processDeleteYesCommand(AppUser user, String input) {
        var hash = input.substring(DELETE_CONFIRM_PREFIX.length());
        var configId = decoder.idOf(hash);
        var optional = configService.getById(configId);

        if (optional.isPresent()) {
            configService.delete(optional.get());
            userService.clearUserState(user.getTelegramId());
            log.debug("User {} deleted configuration with id {} and state set to BASIC_STATE", user.getFirstName(), configId);
            return MESSAGE_CONFIGURATION_DELETED;
        } else {
            log.warn("Configuration with id {} not found for user {}", configId, user.getFirstName());
            return MESSAGE_CONFIGURATION_NOT_FOUND;
        }
    }

    private String processDeleteNoCommand(AppUser user) {
        userService.clearUserState(user.getTelegramId());
        log.debug("User {} chose not to delete the configuration and state set to BASIC_STATE", user.getFirstName());
        return MESSAGE_CONFIGURATION_NOT_DELETED;
    }
}
