package com.bipbup.service.impl;

import com.bipbup.entity.AppUser;
import com.bipbup.enums.AppUserState;
import com.bipbup.handlers.StateHandler;
import com.bipbup.handlers.impl.BasicStateHandler;
import com.bipbup.handlers.impl.QueryDeleteStateHandler;
import com.bipbup.handlers.impl.QueryListStateHandler;
import com.bipbup.handlers.impl.QueryMenuStateHandler;
import com.bipbup.handlers.impl.QueryUpdateStateHandler;
import com.bipbup.handlers.impl.WaitAreaStateHandler;
import com.bipbup.handlers.impl.WaitBroadcastMessageHandler;
import com.bipbup.handlers.impl.WaitConfigNameStateHandler;
import com.bipbup.handlers.impl.WaitEducationStateHandler;
import com.bipbup.handlers.impl.WaitExperienceStateHandler;
import com.bipbup.handlers.impl.WaitQueryStateHandler;
import com.bipbup.handlers.impl.WaitScheduleStateHandler;
import com.bipbup.service.AnswerProducer;
import com.bipbup.service.MainService;
import com.bipbup.service.UserService;
import com.bipbup.utils.factory.KeyboardMarkupFactory;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;

import java.util.Map;
import java.util.Set;

import static com.bipbup.enums.AppUserState.BASIC_STATE;
import static com.bipbup.enums.AppUserState.QUERY_DELETE_STATE;
import static com.bipbup.enums.AppUserState.QUERY_LIST_STATE;
import static com.bipbup.enums.AppUserState.QUERY_MENU_STATE;
import static com.bipbup.enums.AppUserState.QUERY_UPDATE_STATE;
import static com.bipbup.enums.AppUserState.WAIT_AREA_STATE;
import static com.bipbup.enums.AppUserState.WAIT_BROADCAST_MESSAGE;
import static com.bipbup.enums.AppUserState.WAIT_CONFIG_NAME_STATE;
import static com.bipbup.enums.AppUserState.WAIT_EDUCATION_STATE;
import static com.bipbup.enums.AppUserState.WAIT_EXPERIENCE_STATE;
import static com.bipbup.enums.AppUserState.WAIT_QUERY_STATE;
import static com.bipbup.enums.AppUserState.WAIT_SCHEDULE_STATE;
import static com.bipbup.utils.CommandMessageConstants.Prefix;


@Service
public class MainServiceImpl implements MainService {

    private final UserService userService;

    private final KeyboardMarkupFactory markupFactory;

    private final AnswerProducer answerProducer;

    private final Map<AppUserState, StateHandler> messageStateHandlers;

    private final Set<CallbackHandlerProperties> callbackHandlerProperties;

    public MainServiceImpl(final UserService userService,
                           final KeyboardMarkupFactory markupFactory,
                           final AnswerProducer answerProducer,
                           final BasicStateHandler basicStateHandler,
                           final WaitConfigNameStateHandler waitConfigNameStateHandle,
                           final WaitQueryStateHandler waitQueryStateHandler,
                           final QueryListStateHandler queryListStateHandler,
                           final QueryMenuStateHandler queryMenuStateHandler,
                           final QueryDeleteStateHandler queryDeleteStateHandler,
                           final QueryUpdateStateHandler queryUpdateStateHandler,
                           final WaitExperienceStateHandler waitExperienceStateHandler,
                           final WaitAreaStateHandler waitAreaStateHandler,
                           final WaitEducationStateHandler waitEducationStateHandler,
                           final WaitScheduleStateHandler waitScheduleStateHandler,
                           final WaitBroadcastMessageHandler waitBroadcastMessageHandler) {
        this.userService = userService;
        this.markupFactory = markupFactory;
        this.answerProducer = answerProducer;

        this.messageStateHandlers = Map.of(
                BASIC_STATE, basicStateHandler,
                WAIT_CONFIG_NAME_STATE, waitConfigNameStateHandle,
                WAIT_QUERY_STATE, waitQueryStateHandler,
                WAIT_AREA_STATE, waitAreaStateHandler,
                WAIT_BROADCAST_MESSAGE, waitBroadcastMessageHandler
        );

        this.callbackHandlerProperties = Set.of(
                new CallbackHandlerProperties(QUERY_LIST_STATE, queryListStateHandler, Prefix.QUERY),
                new CallbackHandlerProperties(QUERY_MENU_STATE, queryMenuStateHandler, Prefix.MENU_STATE),
                new CallbackHandlerProperties(QUERY_DELETE_STATE, queryDeleteStateHandler, Prefix.DELETE_STATE),
                new CallbackHandlerProperties(QUERY_UPDATE_STATE, queryUpdateStateHandler, Prefix.UPDATE_STATE),
                new CallbackHandlerProperties(WAIT_EXPERIENCE_STATE, waitExperienceStateHandler, Prefix.WAIT_EXP_STATE),
                new CallbackHandlerProperties(WAIT_EDUCATION_STATE, waitEducationStateHandler, Prefix.WAIT_EDU_STATE),
                new CallbackHandlerProperties(WAIT_SCHEDULE_STATE, waitScheduleStateHandler, Prefix.WAIT_SCHEDULE_STATE)
        );
    }

    @Override
    public void processMessage(final Update update) {
        var text = update.getMessage().getText();
        var user = userService.findOrSaveAppUser(update);
        var userState = userService.getUserState(user.getTelegramId());
        var handler = messageStateHandlers.getOrDefault(userState, messageStateHandlers.get(BASIC_STATE));
        var output = handler.process(user, text);

        if (!output.isEmpty())
            processMessageOutput(user, output);
    }

    @Override
    public void processCallbackQuery(final Update update) {
        var callbackData = update.getCallbackQuery().getData();
        var user = userService.findOrSaveAppUser(update);
        var state = userService.getUserState(user.getTelegramId());

        if (callbackData.equals("delete_me_from_db")) {
            userService.deleteUser(user);
            return;
        }

        var optionalProperty = callbackHandlerProperties.stream()
                .filter(p -> p.state().equals(state))
                .findFirst();

        var handler = optionalProperty.isPresent()
                ? optionalProperty.get().handler()
                : getCallbackStateHandler(callbackData);

        var output = handler.process(user, callbackData);

        if (!output.isEmpty())
            processCallbackQueryOutput(user, update.getCallbackQuery(), output);
    }

    private StateHandler getCallbackStateHandler(final String callbackData) {
        var prefix = extractPrefix(callbackData);

        var optionalProperty = callbackHandlerProperties.stream()
                .filter(p -> p.prefix().equals(prefix))
                .findFirst();

        return optionalProperty.isPresent()
                ? optionalProperty.get().handler()
                : messageStateHandlers.get(BASIC_STATE);
    }

    private String extractPrefix(String callbackData) {
        return callbackData.substring(0, callbackData.indexOf('_') + 1);
    }

    private void processMessageOutput(final AppUser user, final String output) {
        var telegramId = user.getTelegramId();

        sendAnswer(output, telegramId, fetchKeyboardWithoutCallback(user));
    }

    private void processCallbackQueryOutput(final AppUser user,
                                            final CallbackQuery callbackQuery,
                                            final String output) {
        var callbackData = callbackQuery.getData();
        var telegramId = user.getTelegramId();
        var messageId = callbackQuery.getMessage().getMessageId();
        var state = userService.getUserState(telegramId);

        if (!state.isWaiting() || isMultiSelecting(callbackData))
            editAnswer(output, telegramId, messageId, fetchKeyboardWithCallback(user, callbackData));
        else
            sendAnswer(output, telegramId, fetchKeyboardWithCallback(user, callbackData));
    }

    private static boolean isMultiSelecting(final String callbackData) {
        return callbackData.startsWith(Prefix.WAIT_EDU_STATE)
                || callbackData.startsWith(Prefix.WAIT_SCHEDULE_STATE);
    }

    private void sendAnswer(final String text,
                            final Long chatId,
                            final ReplyKeyboard keyboard) {
        var sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .replyMarkup(keyboard != null ? keyboard : new ReplyKeyboardRemove(true))
                .parseMode("Markdown")
                .build();

        answerProducer.produceAnswer(sendMessage);
    }

    private void editAnswer(final String output,
                            final Long chatId,
                            final Integer messageId,
                            final InlineKeyboardMarkup markup) {
        var messageText = EditMessageText.builder()
                .text(output)
                .chatId(chatId)
                .messageId(messageId)
                .replyMarkup(markup)
                .parseMode("Markdown")
                .build();

        answerProducer.produceEdit(messageText);
    }

    private InlineKeyboardMarkup fetchKeyboardWithCallback(final AppUser user, final String callbackData) {
        var userState = userService.getUserState(user.getTelegramId());

        return switch (userState) {
            case QUERY_MENU_STATE -> markupFactory.createConfigManagementKeyboard(callbackData);
            case QUERY_DELETE_STATE -> markupFactory.createDeleteConfirmationKeyboard(callbackData);
            case QUERY_UPDATE_STATE -> markupFactory.createUpdateConfigKeyboard(callbackData);
            case WAIT_EXPERIENCE_STATE -> markupFactory.createExperienceSelectionKeyboard(callbackData);
            case WAIT_EDUCATION_STATE -> markupFactory.createEducationLevelSelectionKeyboard(user, callbackData);
            case WAIT_SCHEDULE_STATE -> markupFactory.createScheduleTypeSelectionKeyboard(user, callbackData);
            case QUERY_LIST_STATE -> markupFactory.createUserConfigListKeyboard(user);
            default -> null;
        };
    }

    private InlineKeyboardMarkup fetchKeyboardWithoutCallback(final AppUser user) {
        var userState = userService.getUserState(user.getTelegramId());

        if (userState.equals(QUERY_LIST_STATE))
            return markupFactory.createUserConfigListKeyboard(user);

        return null;
    }
}
