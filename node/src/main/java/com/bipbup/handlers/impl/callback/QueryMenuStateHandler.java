package com.bipbup.handlers.impl.callback;

import com.bipbup.annotation.CallbackQualifier;
import com.bipbup.entity.AppUser;
import com.bipbup.entity.AppUserConfig;
import com.bipbup.entity.EducationLevel;
import com.bipbup.entity.ScheduleType;
import com.bipbup.enums.AppUserState;
import com.bipbup.enums.EnumParam;
import com.bipbup.handlers.StateHandler;
import com.bipbup.handlers.impl.message.BasicStateHandler;
import com.bipbup.service.cache.UserStateCacheService;
import com.bipbup.service.db.ConfigService;
import com.bipbup.utils.Decoder;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


import static com.bipbup.enums.AppUserState.QUERY_DELETE_STATE;
import static com.bipbup.enums.AppUserState.QUERY_MENU_STATE;
import static com.bipbup.enums.AppUserState.QUERY_UPDATE_STATE;
import static com.bipbup.utils.CommandMessageConstants.BotCommand.MYQUERIES;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.CONFIG_NOT_FOUND;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.DELETE_CONFIRMATION;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.MENU_AREA;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.MENU_CONFIG_NAME;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.MENU_EDUCATION;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.MENU_EXCLUSION;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.MENU_EXPERIENCE;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.MENU_QUERY;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.MENU_SCHEDULE;
import static com.bipbup.utils.CommandMessageConstants.Prefix;

@Slf4j
@Component
@CallbackQualifier
@RequiredArgsConstructor
public class QueryMenuStateHandler implements StateHandler {

	private final UserStateCacheService userStateCacheService;

	private final ConfigService configService;

	private final Decoder decoder;

	private final BasicStateHandler basicStateHandler;

	@Override
	public String process(AppUser user, String input) {
		if (isBackToQueryListCommand(input))
			return processBackToQueryListCommand(user);
		if (hasDeletePrefix(input))
			return processConfigActionCommand(user, input, QUERY_DELETE_STATE);
		if (hasUpdatePrefix(input))
			return processConfigActionCommand(user, input, QUERY_UPDATE_STATE);

		return "";
	}

	@Override
	public AppUserState state() {
		return QUERY_MENU_STATE;
	}

	private String processBackToQueryListCommand(AppUser user) {
		return basicStateHandler.process(user, MYQUERIES.toString());
	}

	private boolean isBackToQueryListCommand(String input) {
		return MYQUERIES.toString().equals(input);
	}

	private boolean hasUpdatePrefix(String input) {
		return input.startsWith(Prefix.UPDATE);
	}

	private boolean hasDeletePrefix(String input) {
		return input.startsWith(Prefix.DELETE);
	}

	private void appendEnumParams(StringBuilder output, List<? extends EnumParam> values, String prefix) {
		if (!values.isEmpty()) {
			output.append('\n')
					.append(prefix)
					.append('\n');

			String paramNames = values.stream()
					.map(param -> "  - " + param.getDescription())
					.sorted()
					.collect(Collectors.joining("\n"));

			output.append(paramNames);
		}
	}

	private String showDetailedQueryOutput(AppUserConfig config) {
		StringBuilder output = new StringBuilder().append(MENU_CONFIG_NAME)
				.append(config.getConfigName()).append("\n")
				.append(MENU_QUERY)
				.append(config.getQueryText()).append("\n")
				.append(MENU_EXPERIENCE)
				.append(config.getExperience().getDescription()).append("\n");

		if (config.getArea() != null) {
			output.append(MENU_AREA)
					.append(config.getArea())
					.append("\n");
		}

		if (config.getExclusion() != null) {
			output.append(MENU_EXCLUSION)
					.append(config.getExclusion())
					.append("\n");
		}

		var eduParams = config.getEducationLevels()
				.stream()
				.map(EducationLevel::getParam)
				.toList();

		var scheduleParams = config.getScheduleTypes()
				.stream()
				.map(ScheduleType::getParam)
				.toList();

		appendEnumParams(output, eduParams, MENU_EDUCATION.toString());
		appendEnumParams(output, scheduleParams, MENU_SCHEDULE.toString());

		return output.toString();
	}

	private String processConfigActionCommand(AppUser user, String input, AppUserState state) {
		var configId = decoder.parseIdFromCallback(input);
		var optionalConfig = configService.getConfigById(configId);

		if (optionalConfig.isPresent()) {
			var config = optionalConfig.get();
			userStateCacheService.putUserState(user.getTelegramId(), state);
			log.info("User {} selected menu action and state set to {}", user.getFirstName(), state);

			if (state == QUERY_UPDATE_STATE)
				return showDetailedQueryOutput(config);

			return String.format(DELETE_CONFIRMATION.toString(), config.getConfigName());
		} else {
			log.debug("Configuration with id {} not found for user {}", configId, user.getFirstName());
			return CONFIG_NOT_FOUND.toString();
		}
	}
}
