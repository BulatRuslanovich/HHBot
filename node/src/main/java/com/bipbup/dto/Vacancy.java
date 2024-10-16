package com.bipbup.dto;

import java.util.List;


public record Vacancy(
		String name,
		Area area,
		String  publishedAt,
		String alternateUrl,
		Employer employer,
		Schedule schedule,
		List<ProfessionalRole> professionalRoles,
		Experience experience,
		Employment employment
) {
	public record Area(
			String name
	) {}

	public record Employer(
			String name,
			boolean trusted
	) {}

	public record Schedule(
			String name
	) {}

	public record ProfessionalRole(
			String name
	) {}

	public record Experience(
			String name
	) {}

	public record Employment(
			String name
	) {}
}
