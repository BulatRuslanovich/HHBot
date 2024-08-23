package com.bipbup.utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

public class CommandMessageConstants {

    public static final String DELETE_CANCEL_COMMAND = Prefix.DELETE_STATE + "cancel";
    public static final String ANY = "Любой";

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

        WELCOME("🎉 Добро пожаловать в мир возможностей, %s! Пора завоевывать новые горизонты!"),

        QUERY_PROMPT("📝 Как вы назовете свою конфигурацию? Если передумали, просто введите /cancel."),

        USER_QUERIES("📋 Вот ваши сохраненные запросы:"),

        NO_SAVED_QUERIES("""
                ℹ️ У вас ещё нет сохранённых запросов.
                Но это легко исправить — введите /newquery, чтобы начать!
                """),

        QUERY_OUTPUT("""
                🔍 Конфигурация *%s* с запросом *%s*.
                Что будем с этим делать?
                """),

        DELETE_CONFIRMATION("❗ Вы точно хотите удалить этот запрос? Это действие нельзя будет отменить."),

        CONFIG_DELETED("❌ Конфигурация была успешно удалена."),

        CONFIG_NOT_DELETED("✅ Конфигурация не была удалена."),

        CONFIG_EXISTS("⚠️ Конфигурация с названием *%s* уже существует."),

        CONFIG_NAME_UPDATED("Название конфигурации *%s* успешно изменено на *%s*."),

        QUERY_SET("Запрос *%s* успешно установлен в конфигурации *%s*."),

        EXP_SET("Опыт работы *%s* успешно установлен в конфигурации *%s*."),

        AREA_SET("Регион *%s* успешно установлен в конфигурации *%s*."),

        ANY_AREA_SET("Регион не будет учитываться в конфигурации *%s*."),

        EDU_SAVE("Уровень образования успешно сохранен для конфигурации *%s*."),

        SCHEDULE_SAVE("График работы успешно сохранен для конфигурации *%s*."),

        CONFIG_NOT_FOUND("❌ Конфигурация не найдена."),

        INVALID_INPUT("⚠️ Некорректный ввод. Пожалуйста, проверьте введенные данные."),

        ENTER_CONFIG_NAME("Введите новое название для конфигурации *%s*:"),

        ENTER_QUERY("Введите запрос для конфигурации *%s*:"),

        ENTER_AREA("Введите название региона или _Любой_ для конфигурации *%s*:"),

        SELECT_EXPERIENCE("Выберите опыт работы для конфигурации *%s*:"),

        SELECT_EDUCATION("Выберите уровень образования для конфигурации *%s*:"),

        SELECT_SCHEDULE("Выберите график работы для конфигурации *%s*:"),

        COMMAND_CANCELLED("❌ Действие отменено. Если передумаете, всегда можно начать заново!");

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

        public static final String UPDATE = "🔄 Обновить";

        public static final String DELETE = "🗑️ Удалить";

        public static final String DELETE_CONFIRM = "❌ Удалить";

        public static final String DELETE_CANCEL = "✅ Сохранить";

        public static final String UPDATE_CONFIG_NAME = "Переименовать конфигурацию";

        public static final String UPDATE_QUERY = "Обновить запрос";

        public static final String UPDATE_AREA = "Выбрать новый регион";

        public static final String UPDATE_EXPERIENCE = "Изменить опыт работы";

        public static final String UPDATE_EDUCATION = "Изменить образование";

        public static final String UPDATE_SCHEDULE = "Изменить график";

        public static final String BACK = "🔙 Вернуться";

        public static final String SELECTED = " \uD83D\uDD18";

        public static final String SAVE = "💾 Сохранить";

        private ButtonText() {
        }
    }

    private CommandMessageConstants() {
    }
}
