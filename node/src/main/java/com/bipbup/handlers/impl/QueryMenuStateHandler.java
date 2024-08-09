package com.bipbup.handlers.impl;

import com.bipbup.dao.AppUserDAO;
import com.bipbup.entity.AppUser;
import com.bipbup.handlers.StateHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.bipbup.enums.AppUserState.BASIC_STATE;
import static com.bipbup.enums.AppUserState.QUERY_DELETE_STATE;

@Slf4j
@RequiredArgsConstructor
@Component
public class QueryMenuStateHandler implements StateHandler {
    protected static final String COMMAND_CANCEL = "/cancel";
    protected static final String COMMAND_BACK_TO_QUERY_LIST = "back_to_query_list";
    protected static final String COMMAND_MY_QUERIES = "/myqueries";
    protected static final String COMMAND_NEW_QUERY = "/newquery";
    protected static final String PREFIX_DELETE = "delete_";
    protected static final String MESSAGE_COMMAND_CANCELLED = "Команда отменена!";
    protected static final String MESSAGE_DELETE_CONFIRMATION = "Вы уверены, что хотите удалить этот запрос?";

    private final BasicStateHandler basicStateHandler;
    private final AppUserDAO appUserDAO;

    @Override
    public String process(AppUser appUser, String text) {
        return switch (text) {
            case COMMAND_CANCEL -> handleCancelCommand(appUser);
            case COMMAND_BACK_TO_QUERY_LIST, COMMAND_MY_QUERIES -> handleMyQueriesCommand(appUser);
            case COMMAND_NEW_QUERY -> handleNewQueryCommand(appUser);
            default -> {
                if (text.startsWith(PREFIX_DELETE)) {
                    yield handleDeleteCommand(appUser);
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
}
