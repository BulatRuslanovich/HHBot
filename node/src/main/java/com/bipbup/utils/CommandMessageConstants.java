package com.bipbup.utils;

public class CommandMessageConstants {

    // callback prefixes and commands
    public static final String QUERY_PREFIX = "query_";

    public static final String MENU_STATE_PREFIX = "action_";
    public static final String UPDATE_PREFIX = MENU_STATE_PREFIX + "update_";
    public static final String DELETE_PREFIX = MENU_STATE_PREFIX + "delete_";

    public static final String UPDATE_STATE_PREFIX = "update_";
    public static final String UPDATE_CONFIG_NAME_PREFIX = UPDATE_STATE_PREFIX + "config_name_";
    public static final String UPDATE_QUERY_PREFIX = UPDATE_STATE_PREFIX + "query_";
    public static final String UPDATE_AREA_PREFIX = UPDATE_STATE_PREFIX + "area_";
    public static final String UPDATE_EXPERIENCE_PREFIX = UPDATE_STATE_PREFIX + "experience_";
    public static final String UPDATE_EDUCATION_PREFIX = UPDATE_STATE_PREFIX + "education_";
    public static final String UPDATE_SCHEDULE_PREFIX = UPDATE_STATE_PREFIX + "schedule_";

    public static final String DELETE_STATE_PREFIX = "delete_";
    public static final String DELETE_CONFIRM_PREFIX = DELETE_STATE_PREFIX + "confirm_";
    public static final String DELETE_CANCEL_COMMAND = DELETE_STATE_PREFIX + "cancel";

    public static final String WAIT_EXP_STATE_PREFIX = "exp_";
    public static final String EXP_NOT_IMPORTANT_PREFIX = WAIT_EXP_STATE_PREFIX + "not_important_";
    public static final String NO_EXP_PREFIX = WAIT_EXP_STATE_PREFIX + "no_";
    public static final String EXP_1_3_YEARS_PREFIX = WAIT_EXP_STATE_PREFIX + "1_3_years_";
    public static final String EXP_3_6_YEARS_PREFIX = WAIT_EXP_STATE_PREFIX + "3_6_years_";
    public static final String EXP_MORE_6_YEARS_PREFIX = WAIT_EXP_STATE_PREFIX + "more_6_years_";


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
    public static final String CONFIG_NAME_UPDATED_MESSAGE = "Название конфигурации изменено.";
    public static final String INVALID_CONFIG_NAME_MESSAGE = "Некорректное название. Пожалуйста, проверьте введенные данные.";

    // wait query state messages
    public static final String INVALID_QUERY_MESSAGE = "Некорректный запрос. Пожалуйста, проверьте введенные данные.";
    public static final String QUERY_SET_MESSAGE_TEMPLATE = "Запрос \"%s\" успешно установлен в конфигурации \"%s\".";

    // wait experience state messages
    public static final String EXP_SET_MESSAGE_TEMPLATE = "Опыт работы \"%s\" успешно установлен в конфигурации \"%s\".";

    // error messages
    public static final String CONFIG_NOT_FOUND_MESSAGE = "Конфигурация не найдена.";
    public static final String PROCESSING_COMMAND_ERROR_MESSAGE = "Ошибка при обработке команды. Попробуйте еще раз.";
    public static final String UNEXPECTED_ERROR_MESSAGE = "Произошла ошибка. Попробуйте еще раз.";

    // query menu state button texts
    public static final String BUTTON_TEXT_UPDATE = "Обновить";
    public static final String BUTTON_TEXT_DELETE = "Удалить";
    public static final String BUTTON_TEXT_BACK = "Назад";

    // query delete state button texts
    public static final String BUTTON_TEXT_DELETE_CONFIRM = "Да, удалить";
    public static final String BUTTON_TEXT_DELETE_CANCEL = "Нет, не удалять";

    // query update state button texts
    public static final String BUTTON_TEXT_UPDATE_CONFIG_NAME = "Изменить название";
    public static final String BUTTON_TEXT_UPDATE_QUERY = "Изменить запрос";
    public static final String BUTTON_TEXT_UPDATE_AREA = "Изменить регион";
    public static final String BUTTON_TEXT_UPDATE_EXPERIENCE = "Изменить опыт работы";
    public static final String BUTTON_TEXT_UPDATE_EDUCATION = "Изменить уровень образования";
    public static final String BUTTON_TEXT_UPDATE_SCHEDULE = "Изменить график работы";

    // wait experience state button texts
    public static final String BUTTON_TEXT_EXP_NOT_IMPORTANT = "Не имеет значения";
    public static final String BUTTON_TEXT_NO_EXP = "Нет опыта";
    public static final String BUTTON_TEXT_EXP_1_3_YEARS = "От 1 года до 3 лет";
    public static final String BUTTON_TEXT_EXP_3_6_YEARS = "От 3 до 6 лет";
    public static final String BUTTON_TEXT_EXP_MORE_6_YEARS = "Более 6 лет";

    private CommandMessageConstants() {
    }
}