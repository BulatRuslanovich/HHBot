package com.bipbup.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor(staticName = "of")
public class VacancyDTO {

    private String headHunterId;

    private String nameVacancy;

    private String nameEmployer;

    private String nameArea;

    private LocalDateTime publishedAt;

    private String url;

    private String  professionalRoles;

    private String schedule;

    private String experience;

    private String employment;
}
