package com.bipbup.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;


import java.time.LocalDateTime;

@Data
@AllArgsConstructor(staticName = "of")
public class VacancyDTO {
    @NonNull
    private String headHunterId;
    @NonNull
    private String nameVacancy;
    @NonNull
    private String nameEmployer;
    @NonNull
    private String nameArea;
    @NonNull
    private LocalDateTime publishedAt;
    @NonNull
    private String url;
}
