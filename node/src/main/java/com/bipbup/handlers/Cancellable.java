package com.bipbup.handlers;

import com.bipbup.entity.AppUser;
import com.bipbup.handlers.impl.BasicStateHandler;
import com.bipbup.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.bipbup.utils.CommandMessageConstants.CANCEL_COMMAND;
import static com.bipbup.utils.CommandMessageConstants.COMMAND_CANCELLED_MESSAGE;
import static com.bipbup.utils.CommandMessageConstants.MYQUERIES_COMMAND;
import static com.bipbup.utils.CommandMessageConstants.NEWQUERY_COMMAND;

@Slf4j
@RequiredArgsConstructor
public abstract class Cancellable {

    protected final UserService userService;

    private final BasicStateHandler basicStateHandler;

    protected boolean isCancelCommand(final String input) {
        return CANCEL_COMMAND.equals(input);
    }

    protected boolean isBasicCommand(String input) {
        return MYQUERIES_COMMAND.equals(input)
                || NEWQUERY_COMMAND.equals(input);
    }

    protected String processCancelCommand(final AppUser user) {
        userService.clearUserState(user.getTelegramId());
        log.debug("User {} cancelled the command and state set to BASIC_STATE.", user.getFirstName());
        return COMMAND_CANCELLED_MESSAGE;
    }

    protected String processBasicCommand(AppUser user, String input) {
        return basicStateHandler.process(user, input);
    }
}
