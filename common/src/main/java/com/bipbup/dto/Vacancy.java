package com.bipbup.dto;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@AllArgsConstructor(staticName = "of")
public class Vacancy {
    @NonNull
    String headHunterId;
    @NonNull
    String nameVacancy;
    @NonNull
    String nameEmployer;
    @NonNull
    String nameArea;
    @NonNull
    LocalDateTime publishedAt;
    @NonNull
    String url;
}