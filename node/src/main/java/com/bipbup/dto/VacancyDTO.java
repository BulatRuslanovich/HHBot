package com.bipbup.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor(staticName = "of")
public class VacancyDTO {

    private String headHunterId;

    private String nameVacancy;

    private String nameEmployer;

    private String nameArea;

    private LocalDateTime publishedAt;

    private String url;
}
