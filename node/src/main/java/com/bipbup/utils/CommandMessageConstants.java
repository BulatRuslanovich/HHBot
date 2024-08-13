package com.bipbup.utils;

public class CommandMessageConstants {

    // callback prefixes and commands
    public static final String QUERY_PREFIX = "query_";
    public static final String UPDATE_PREFIX = "action_update_";
    public static final String DELETE_PREFIX = "action_delete_";
    public static final String UPDATE_CONFIG_NAME_PREFIX = "update_config_name_";
    public static final String UPDATE_QUERY_PREFIX = "update_query_";
    public static final String UPDATE_AREA_PREFIX = "update_area_";
    public static final String UPDATE_EXPERIENCE_PREFIX = "update_experience_";
    public static final String UPDATE_EDUCATION_PREFIX = "update_education_";
    public static final String UPDATE_SCHEDULE_PREFIX = "update_schedule_";
    public static final String DELETE_CONFIRM_PREFIX = "delete_confirm_";
    public static final String DELETE_CANCEL_COMMAND = "delete_cancel";

    // cancellable messages and commands
    public static final String COMMAND_CANCELLED_MESSAGE = "Команда была отменена.";
    public static final String CANCEL_COMMAND = "/cancel";

    // basic state commands
    public static final String START_COMMAND = "/start";
    public static final String NEWQUERY_COMMAND = "/newquery";
    public static final String MYQUERIES_COMMAND = "/myqueries";

    // basic state messages
    public static final String WELCOME_MESSAGE = "Добро пожаловать в капитализм, %s!";
    public static final String QUERY_PROMPT_MESSAGE = "Введите название вашей конфигурации, если хотите отменить команду, пожалуйста, введите /cancel:";
    public static final String USER_QUERIES_MESSAGE = "Ваши запросы:";
    public static final String NO_SAVED_QUERIES_MESSAGE = """
            У вас пока нет сохранённых запросов.
            Введите /newquery, чтобы добавить новый запрос.
            """;

    // query list state messages
    public static final String QUERY_OUTPUT_MESSAGE_TEMPLATE = """
            Конфигурация "%s" с запросом "%s"
            Что хотите сделать с ней?""";

    // query menu state messages
    public static final String DELETE_CONFIRMATION_MESSAGE = "Вы уверены, что хотите удалить этот запрос?";

    // query delete state messages
    public static final String CONFIG_DELETED_MESSAGE = "Конфигурация была удалена.";
    public static final String CONFIG_NOT_DELETED_MESSAGE = "Конфигурация не была удалена.";

    // query update state messages
    public static final String ENTER_CONFIG_NAME_MESSAGE = "Введите новое название конфигурации:";
    public static final String ENTER_QUERY_MESSAGE = "Введите новый запрос:";
    public static final String ENTER_AREA_MESSAGE = "Введите название региона:";
    public static final String SELECT_EXPERIENCE_MESSAGE = "Выберите опыт работы:";
    public static final String SELECT_EDUCATION_MESSAGE = "Выберите уровень образования:";
    public static final String SELECT_SCHEDULE_MESSAGE = "Выберите график работы:";

    // wait config name state messages
    public static final String CONFIG_EXISTS_MESSAGE_TEMPLATE = "Конфигурация с названием \"%s\" уже существует.";
    public static final String ENTER_QUERY_MESSAGE_TEMPLATE = "Введите запрос для конфигурации \"%s\":";
    public static final String CONFIG_UPDATED_MESSAGE = "Название конфигурации изменено.";
    public static final String INVALID_CONFIG_NAME_MESSAGE = "Некорректное название. Пожалуйста, проверьте введенные данные.";

    // wait query state messages
    public static final String INVALID_QUERY_MESSAGE = "Некорректный запрос. Пожалуйста, проверьте введенные данные.";
    public static final String QUERY_SET_MESSAGE_TEMPLATE = "Запрос \"%s\" успешно установлен в конфигурации \"%s\".";

    // error messages
    public static final String CONFIG_NOT_FOUND_MESSAGE = "Конфигурация не найдена.";
    public static final String PROCESSING_COMMAND_ERROR_MESSAGE = "Ошибка при обработке команды. Попробуйте еще раз.";
    public static final String UNEXPECTED_ERROR_MESSAGE = "Произошла ошибка. Попробуйте еще раз.";

    // button texts
    public static final String BUTTON_TEXT_UPDATE = "Обновить";
    public static final String BUTTON_TEXT_DELETE = "Удалить";
    public static final String BUTTON_TEXT_BACK = "Назад";
    public static final String BUTTON_TEXT_DELETE_CONFIRM = "Да, удалить";
    public static final String BUTTON_TEXT_DELETE_CANCEL = "Нет, не удалять";
    public static final String BUTTON_TEXT_UPDATE_CONFIG_NAME = "Изменить название";
    public static final String BUTTON_TEXT_UPDATE_QUERY = "Изменить запрос";
    public static final String BUTTON_TEXT_UPDATE_AREA = "Изменить регион";
    public static final String BUTTON_TEXT_UPDATE_EXPERIENCE = "Изменить опыт работы";
    public static final String BUTTON_TEXT_UPDATE_EDUCATION = "Изменить уровень образования";
    public static final String BUTTON_TEXT_UPDATE_SCHEDULE = "Изменить график работы";

    private CommandMessageConstants() {
    }
}