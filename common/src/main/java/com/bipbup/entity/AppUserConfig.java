package com.bipbup.entity;

import com.bipbup.enums.EducationLevelParam;
import com.bipbup.enums.ExperienceParam;
import com.bipbup.enums.ScheduleTypeParam;
import io.hypersistence.utils.hibernate.type.array.EnumArrayType;
import io.hypersistence.utils.hibernate.type.array.internal.AbstractArrayType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = "userConfigId")
@Table(name = "app_user_config")
@Entity
public class AppUserConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userConfigId;

    private String configName;

    private String queryText;

    private String region;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private ExperienceParam experience = ExperienceParam.NO_MATTER;

    @Type(
            value = EnumArrayType.class,
            parameters = @Parameter(name = AbstractArrayType.SQL_ARRAY_TYPE,
                    value = "education_param")
    )
    @Column(
            name = "education_params",
            columnDefinition = "education_param[]"
    )
    private EducationLevelParam[] educationLevels;

    @Type(
            value = EnumArrayType.class,
            parameters = @Parameter(name = AbstractArrayType.SQL_ARRAY_TYPE,
                    value = "schedule_param")
    )
    @Column(
            name = "schedule_params",
            columnDefinition = "schedule_param[]"
    )
    private ScheduleTypeParam[] scheduleTypes;

    @Builder.Default
    private LocalDateTime lastNotificationTime = LocalDateTime.now().minusDays(1);

    @ManyToOne
    @JoinColumn(name = "app_user_id")
    private AppUser appUser;
}
