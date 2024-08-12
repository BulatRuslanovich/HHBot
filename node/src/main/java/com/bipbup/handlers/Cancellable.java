package com.bipbup.handlers;

import com.bipbup.entity.AppUser;
import com.bipbup.handlers.impl.BasicStateHandler;
import com.bipbup.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public abstract class Cancellable {

    protected final UserService userService;

    private final BasicStateHandler basicStateHandler;

    protected static final String MESSAGE_COMMAND_CANCELLED = "Команда была отменена.";
    protected static final String CANCEL_COMMAND = "/cancel";
    protected static final String MYQUERIES_COMMAND = "/myqueries";
    protected static final String NEWQUERY_COMMAND = "/newquery";
    protected static final String BACK_TO_QUERY_LIST_COMMAND = "back_to_query_list";

    protected boolean isCancelCommand(final String input) {
        return CANCEL_COMMAND.equals(input);
    }

    protected boolean isBasicCommand(String input) {
        return BACK_TO_QUERY_LIST_COMMAND.equals(input)
                || MYQUERIES_COMMAND.equals(input)
                || NEWQUERY_COMMAND.equals(input);
    }

    protected String processCancelCommand(final AppUser user) {
        userService.clearUserState(user.getTelegramId());
        log.debug("User {} cancelled the command and state set to BASIC_STATE.", user.getFirstName());
        return MESSAGE_COMMAND_CANCELLED;
    }

    protected String processBasicCommand(AppUser user, String input) {
        input = input.equals(BACK_TO_QUERY_LIST_COMMAND) ? MYQUERIES_COMMAND : input;
        return basicStateHandler.process(user, input);
    }
}
