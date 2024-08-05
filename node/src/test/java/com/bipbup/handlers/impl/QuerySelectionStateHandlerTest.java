package com.bipbup.handlers.impl;

import com.bipbup.dao.AppUserConfigDAO;
import com.bipbup.entity.AppUser;
import com.bipbup.entity.AppUserConfig;
import com.bipbup.enums.impl.EducationLevelParam;
import com.bipbup.enums.impl.ExperienceParam;
import com.bipbup.enums.impl.ScheduleTypeParam;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class QuerySelectionStateHandlerTest {

    @Mock
    private BasicStateHandler basicStateHandler;

    @Mock
    private AppUserConfigDAO appUserConfigDAO;

    @InjectMocks
    private QuerySelectionStateHandler querySelectionStateHandler;

    private AppUser appUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        appUser = new AppUser();
        appUser.setFirstName("TestUser");
    }

    @Test
    void testProcessQueryData() {
        long queryId = 123L;
        String callbackData = "query_" + queryId;

        EducationLevelParam[] educationLevels = { EducationLevelParam.HIGHER, EducationLevelParam.SECONDARY_VOCATIONAL };
        ScheduleTypeParam[] scheduleTypes = { ScheduleTypeParam.FULL_DAY, ScheduleTypeParam.REMOTE_WORKING };

        AppUserConfig appUserConfig = AppUserConfig.builder()
                .configName("TestConfig")
                .queryText("Sample Query")
                .region("Region1")
                .experience(ExperienceParam.BETWEEN_1_AND_3)
                .educationLevels(educationLevels)
                .scheduleTypes(scheduleTypes)
                .build();

        when(appUserConfigDAO.findById(queryId)).thenReturn(Optional.of(appUserConfig));

        String result = querySelectionStateHandler.process(appUser, callbackData);

        String expectedOutput = """
                TestConfig
                Текст запроса: Sample Query
                Регион: Region1
                Опыт работы: 1-3 года
                Уровень образования: Высшее, Среднее специальное
                Тип графика: Полный день, Удалённая работа""";

        assertEquals(expectedOutput, result);
    }

    @Test
    void testProcessInvalidQueryData() {
        String callbackData = "query_invalid";

        String result = querySelectionStateHandler.process(appUser, callbackData);

        assertEquals("", result);
    }

    @Test
    void testProcessNewQueryCommand() {
        String callbackData = "/newquery";

        when(basicStateHandler.process(appUser, callbackData)).thenReturn("New Query Message");

        String result = querySelectionStateHandler.process(appUser, callbackData);

        assertEquals("New Query Message", result);
    }

    @Test
    void testProcessBackToQueryListCommand() {
        String callbackData = "back_to_query_list";

        when(basicStateHandler.process(appUser, "/myqueries")).thenReturn("Query List Message");

        String result = querySelectionStateHandler.process(appUser, callbackData);

        assertEquals("Query List Message", result);
    }

    @Test
    void testProcessConfigNotFound() {
        long queryId = 123L;
        String callbackData = "query_" + queryId;

        when(appUserConfigDAO.findById(queryId)).thenReturn(Optional.empty());

        String result = querySelectionStateHandler.process(appUser, callbackData);

        assertEquals("Конфигурация не найдена", result);
    }
}
