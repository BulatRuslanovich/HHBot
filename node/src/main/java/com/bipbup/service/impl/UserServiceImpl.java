package com.bipbup.service.impl;

import com.bipbup.dao.AppUserDAO;
import com.bipbup.entity.AppUser;
import com.bipbup.enums.AppUserState;
import com.bipbup.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import static com.bipbup.enums.AppUserState.BASIC_STATE;

@RequiredArgsConstructor
@Component
public class UserServiceImpl implements UserService {

    private final AppUserDAO appUserDAO;

    @Override
    @Transactional
    public AppUser findOrSaveAppUser(final Update update) {
        var messageSender = update.hasMessage()
                ? update.getMessage().getFrom()
                : update.getCallbackQuery().getFrom();
        var appUserOptional = appUserDAO.findByTelegramId(messageSender.getId());
        return appUserOptional.orElseGet(() -> saveAppUser(messageSender));
    }

    @Override
    @CachePut(value = "userStates", key = "#telegramId")
    public AppUserState saveUserState(Long telegramId, AppUserState state) {
        return state;
    }

    @Override
    @Cacheable(value = "userStates")
    public AppUserState getUserState(Long telegramId) {
        return BASIC_STATE;
    }

    @Override
    @CacheEvict(value = "userStates")
    public void clearUserState(Long telegramId) {
        // clearing cache, doesn't need implementing
    }

    private AppUser saveAppUser(final User messageSender) {
        var appUser = AppUser.builder()
                .telegramId(messageSender.getId())
                .username(messageSender.getUserName())
                .firstName(messageSender.getFirstName())
                .lastName(messageSender.getLastName())
                .build();

        appUserDAO.save(appUser);
        return appUser;
    }
}
