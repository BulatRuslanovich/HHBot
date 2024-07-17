package com.bipbup.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class Vacancy {
    private String headHunterId;
    private String nameVacancy;
    private String nameEmployer;
    private String nameArea;
    private LocalDateTime publishedAt;
    private String url;
}