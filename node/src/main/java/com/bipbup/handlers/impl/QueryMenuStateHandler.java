package com.bipbup.handlers.impl;

import com.bipbup.entity.AppUser;
import com.bipbup.handlers.StateHandler;
import com.bipbup.utils.UserUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.bipbup.enums.AppUserState.BASIC_STATE;
import static com.bipbup.enums.AppUserState.QUERY_DELETE_STATE;

@RequiredArgsConstructor
@Component
public class QueryMenuStateHandler implements StateHandler {
    private final BasicStateHandler basicStateHandler;
    private final UserUtil userUtil;
    @Override
    public String process(AppUser appUser, String text) {
        if (text.equals("/cancel")) {
            userUtil.updateUserState(appUser, BASIC_STATE);
            return "Команда отменена!";
        }

        if (text.equals("back_to_query_list") || text.equals("/myqueries")) {
            return basicStateHandler.process(appUser, "/myqueries");
        }

        if (text.startsWith("delete_")) {
            userUtil.updateUserState(appUser, QUERY_DELETE_STATE);
            return "Вы уверены, что хотите удалить этот запрос?";
        }

        if (text.equals("/newquery")) {
            return basicStateHandler.process(appUser, "/newquery");
        }

        return "";
    }
}
