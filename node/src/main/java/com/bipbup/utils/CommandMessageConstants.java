package com.bipbup.utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import java.util.Locale;

public class CommandMessageConstants {

    static {
        var source = new ReloadableResourceBundleMessageSource();
        source.setBasename("classpath:/messages");
        source.setDefaultEncoding("UTF-8");
        messageSource = source;
    }

    private static final MessageSource messageSource;

    private CommandMessageConstants() {
    }

    private static String get(String code) {
        return messageSource.getMessage(code, null, code, Locale.ROOT);
    }

    public static final String ANY = "Любой";

    @Getter
    @RequiredArgsConstructor
    public enum BotCommand {

        CANCEL("/cancel"),

        START("/start"),

        HELP("/help"),

        NEWQUERY("/newquery"),

        MYQUERIES("/myqueries");

        private final String command;
    }

    @Getter
    @RequiredArgsConstructor
    public enum AdminCommand {

        BROADCAST("!broadcast"),

        SEARCH("!search");

        private final String command;
    }

    @Getter
    @RequiredArgsConstructor
    public enum MessageTemplate {

        WELCOME(get("message.welcome")),

        HELP(get("message.help")),

        QUERY_PROMPT(get("message.query.prompt")),

        USER_QUERIES(get("message.user.queries")),

        NO_SAVED_QUERIES(get("message.no.saved.queries")),

        QUERY_OUTPUT(get("message.query.output")),

        DELETE_CONFIRMATION(get("message.delete.confirmation")),

        CONFIG_DELETED(get("message.config.deleted")),

        CONFIG_EXISTS(get("message.config.exists")),

        CONFIG_NAME_UPDATED(get("message.config.name.updated")),

        QUERY_SET(get("message.query.set")),

        EXP_SET(get("message.exp.set")),

        AREA_SET(get("message.area.set")),

        ANY_AREA_SET(get("message.any.area.set")),

        EDU_SAVE(get("message.edu.save")),

        SCHEDULE_SAVE(get("message.schedule.save")),

        CONFIG_NOT_FOUND(get("message.config.not.found")),

        INVALID_INPUT(get("message.invalid.input")),

        ENTER_CONFIG_NAME(get("message.enter.config.name")),

        ENTER_QUERY(get("message.enter.query")),

        ENTER_AREA(get("message.enter.area")),

        SELECT_EXPERIENCE(get("message.select.experience")),

        SELECT_EDUCATION(get("message.select.education")),

        SELECT_SCHEDULE(get("message.select.schedule")),

        COMMAND_CANCELLED(get("message.command.cancelled")),

        MENU_CONFIG_NAME(get("message.menu.config.name")),

        MENU_QUERY(get("message.menu.config.query")),

        MENU_AREA(get("message.menu.config.area")),

        MENU_EXPERIENCE(get("message.menu.config.experience")),

        MENU_EDUCATION(get("message.menu.config.education")),

        MENU_SCHEDULE(get("message.menu.config.schedule")),

        VACANCY(get("message.vacancy"));

        private final String template;
    }

    @Getter
    @RequiredArgsConstructor
    public enum AdminMessageTemplate {

        NO_PERMISSION(get("admin.message.no.permission")),

        USAGE(get("admin.message.usage")),

        ENTER_MESSAGE(get("admin.message.enter.message")),

        INCORRECT_PASSWORD(get("admin.message.incorrect.password")),

        MESSAGE_SENT(get("admin.message.sent")),

        SEARCHING_COMPLETED(get("admin.message.searching.completed"));

        private final String template;
    }

    // Command prefixes
    public static class Prefix {

        public static final String QUERY = "query_";

        public static final String MENU_STATE = "action_";

        public static final String UPDATE = MENU_STATE + "update_";

        public static final String DELETE = MENU_STATE + "delete_";

        public static final String UPDATE_STATE = "update_";

        public static final String UPDATE_CONFIG_NAME = UPDATE_STATE + "config_name_";

        public static final String UPDATE_QUERY = UPDATE_STATE + "query_";

        public static final String UPDATE_AREA = UPDATE_STATE + "area_";

        public static final String UPDATE_EXPERIENCE = UPDATE_STATE + "experience_";

        public static final String UPDATE_EDUCATION = UPDATE_STATE + "education_";

        public static final String UPDATE_SCHEDULE = UPDATE_STATE + "schedule_";

        public static final String DELETE_STATE = "delete_";

        public static final String DELETE_CONFIRM = DELETE_STATE + "confirm_";

        public static final String WAIT_EXP_STATE = "exp_";

        public static final String WAIT_EDU_STATE = "edu_";

        public static final String WAIT_SCHEDULE_STATE = "schedule_";

        public static final String EDU_SAVE = WAIT_EDU_STATE + "save_";

        public static final String SCHEDULE_SAVE = WAIT_SCHEDULE_STATE + "save_";

        private Prefix() {
        }
    }

    public static class ButtonText {

        public static final String UPDATE = get("button.update");

        public static final String DELETE = get("button.delete");

        public static final String DELETE_CONFIRM = get("button.delete.confirm");

        public static final String DELETE_CANCEL = get("button.delete.cancel");

        public static final String UPDATE_CONFIG_NAME = get("button.update.config.name");

        public static final String UPDATE_QUERY = get("button.update.query");

        public static final String UPDATE_AREA = get("button.update.area");

        public static final String UPDATE_EXPERIENCE = get("button.update.experience");

        public static final String UPDATE_EDUCATION = get("button.update.education");

        public static final String UPDATE_SCHEDULE = get("button.update.schedule");

        public static final String BACK = get("button.back");

        public static final String SELECTED = get("button.selected");

        public static final String SAVE = get("button.save");

        private ButtonText() {
        }
    }
}
