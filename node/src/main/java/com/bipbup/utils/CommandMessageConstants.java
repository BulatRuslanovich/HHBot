package com.bipbup.utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

public class CommandMessageConstants {

    @Getter
    @RequiredArgsConstructor
    public enum BotCommand {
        CANCEL("/cancel"),
        START("/start"),
        NEWQUERY("/newquery"),
        MYQUERIES("/myqueries");

        private final String command;
    }

    @Getter
    @RequiredArgsConstructor
    public enum MessageTemplate {
        WELCOME("Добро пожаловать в капитализм, %s!"),
        QUERY_PROMPT("Введите название вашей конфигурации, если хотите отменить команду, пожалуйста, введите /cancel:"),
        USER_QUERIES("Ваши запросы:"),
        NO_SAVED_QUERIES("""
                У вас пока нет сохранённых запросов.
                Введите /newquery, чтобы добавить новый запрос.
                """),
        QUERY_OUTPUT("""
                Конфигурация "%s" с запросом "%s"
                Что хотите сделать с ней?"""),
        DELETE_CONFIRMATION("Вы уверены, что хотите удалить этот запрос?"),
        CONFIG_DELETED("Конфигурация была удалена."),
        CONFIG_NOT_DELETED("Конфигурация не была удалена."),
        CONFIG_EXISTS("Конфигурация с названием \"%s\" уже существует."),
        CONFIG_NAME_UPDATED("Название конфигурации \"%s\" успешно изменено на \"%s\"."),
        QUERY_SET("Запрос \"%s\" успешно установлен в конфигурации \"%s\"."),
        EXP_SET("Опыт работы \"%s\" успешно установлен в конфигурации \"%s\"."),
        AREA_SET("Регион \"%s\" успешно установлен в конфигурации \"%s\"."),
        EDU_SAVE("Уровень образования успешно сохранен для конфигурации \"%s\"."),
        SCHEDULE_SAVE("График работы успешно сохранен для конфигурации \"%s\"."),
        CONFIG_NOT_FOUND("Конфигурация не найдена."),
        INVALID_INPUT("Некорректный ввод. Пожалуйста, проверьте введенные данные."),
        ENTER_CONFIG_NAME("Введите новое название для конфигурации \"%s\":"),
        ENTER_QUERY("Введите запрос для конфигурации \"%s\":"),
        ENTER_AREA("Введите название региона для конфигурации \"%s\":"),
        SELECT_EXPERIENCE("Выберите опыт работы для конфигурации \"%s\":"),
        SELECT_EDUCATION("Выберите уровень образования для конфигурации \"%s\":"),
        SELECT_SCHEDULE("Выберите график работы для конфигурации \"%s\":"),
        COMMAND_CANCELLED("Команда была отменена.");

        private final String template;
    }

    // Command prefixes
    public static class Prefix {
        private Prefix() {
        }

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

        public static final String EDU_SAVE= WAIT_EDU_STATE + "save_";
        public static final String SCHEDULE_SAVE = WAIT_SCHEDULE_STATE + "save_";

    }

    public static final String DELETE_CANCEL_COMMAND = Prefix.DELETE_STATE + "cancel";

    public static class ButtonText {
        private ButtonText() {
        }

        // query menu state button texts
        public static final String UPDATE = "Обновить";
        public static final String DELETE = "Удалить";

        // query delete state button texts
        public static final String DELETE_CONFIRM = "Да, удалить";
        public static final String DELETE_CANCEL = "Нет, не удалять";

        // query update state button texts
        public static final String UPDATE_CONFIG_NAME = "Изменить название";
        public static final String UPDATE_QUERY = "Изменить запрос";
        public static final String UPDATE_AREA = "Изменить регион";
        public static final String UPDATE_EXPERIENCE = "Изменить опыт работы";
        public static final String UPDATE_EDUCATION = "Изменить уровень образования";
        public static final String UPDATE_SCHEDULE = "Изменить график работы";

        // common button texts
        public static final String BACK = "Назад";
        public static final String SELECTED = " \uD83D\uDD18";
        public static final String SAVE = "Сохранить";
    }

    private CommandMessageConstants() {
    }
}