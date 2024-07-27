package com.bipbup.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class Vacancy {
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