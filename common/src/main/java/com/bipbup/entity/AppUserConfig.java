package com.bipbup.entity;

import com.bipbup.enums.EducationLevelParam;
import com.bipbup.enums.ExperienceParam;
import com.bipbup.enums.ScheduleTypeParam;
import io.hypersistence.utils.hibernate.type.array.EnumArrayType;
import io.hypersistence.utils.hibernate.type.array.internal.AbstractArrayType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
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
            parameters = @Parameter(name = AbstractArrayType.SQL_ARRAY_TYPE, value = "education_level")
    )
    @Column(
            name = "education_levels",
            columnDefinition = "education_level[]"
    )
    private EducationLevelParam[] educationLevels;

    @Type(
            value = EnumArrayType.class,
            parameters = @Parameter(name = AbstractArrayType.SQL_ARRAY_TYPE, value = "schedule_type")
    )
    @Column(
            name = "schedule_types",
            columnDefinition = "schedule_type[]"
    )
    private ScheduleTypeParam[] scheduleTypes;

    @Builder.Default
    private LocalDateTime lastNotificationTime = LocalDateTime.now().minusDays(1);

    @ManyToOne
    @JoinColumn(name = "app_user_id")
    private AppUser appUser;
}
