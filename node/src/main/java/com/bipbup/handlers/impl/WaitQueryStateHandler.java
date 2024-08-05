package com.bipbup.handlers.impl;

import com.bipbup.dao.AppUserConfigDAO;
import com.bipbup.dao.AppUserDAO;
import com.bipbup.entity.AppUser;
import com.bipbup.entity.AppUserConfig;
import com.bipbup.handlers.StateHandler;
import com.bipbup.utils.UserConfigUtil;
import com.bipbup.utils.UserUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

import static com.bipbup.enums.AppUserState.BASIC_STATE;

@Slf4j
@RequiredArgsConstructor
@Component
public class WaitQueryStateHandler implements StateHandler {
    private final UserConfigUtil userConfigUtil;
    private final UserUtil userUtil;

    @Override
    public String process(AppUser appUser, String text) {
        text = text.replace("+", "%2B");
        List<AppUserConfig> configs = appUser.getAppUserConfigs();
        int size = configs.size();
        AppUserConfig lastConfig = configs.get(size - 1);

        if (text.equals("/cancel")) {
            configs.remove(size - 1);
            //appUser.setAppUserConfigs(configs);
            //userUtil.updateUserState(appUser, BASIC_STATE);
            //appUserDAO.save(appUser);
            return "Команда была отменена.";
        }

        if (!isValidQueryText(text)) {
            userUtil.updateUserState(appUser, BASIC_STATE);
            userConfigUtil.deleteConfig(lastConfig);
            return "Некорректный запрос. Пожалуйста, проверьте введенные данные.";
        }

        userConfigUtil.updateConfigQuery(lastConfig, text);
        userUtil.updateUserState(appUser, BASIC_STATE);
        log.info("User {} set query \"{}\"", appUser.getFirstName(), text);
        return String.format("Запрос \"%s\" успешно установлен в конфигурации \"%s\".", text, lastConfig.getConfigName());
    }

    private boolean isValidQueryText(String text) {
        return !Objects.isNull(text) && !text.trim().isEmpty() && text.length() <= 50;
    }
}
