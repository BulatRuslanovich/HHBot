package com.bipbup.dto;

import java.util.List;

public record Area(
		String id,
		String name,
		List<Area> areas
) {}
