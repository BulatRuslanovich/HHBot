package com.bipbup.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DateTimeUtil {

	private final int TIMESTAMP_FULL_LENGTH = 24;

	private final int TIMESTAMP_TRIMMED_LENGTH = 19;

	private final String TIMESTAMP_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";

	public LocalDateTime convertToDate(String timestamp) {
		if (timestamp.length() == TIMESTAMP_FULL_LENGTH)
			timestamp = timestamp.substring(0, TIMESTAMP_TRIMMED_LENGTH);

		return LocalDateTime.parse(timestamp, DateTimeFormatter.ofPattern(TIMESTAMP_PATTERN));
	}
}
