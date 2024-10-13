package com.bipbup.entity;

import com.bipbup.enums.impl.ExperienceParam;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import static java.time.LocalDateTime.now;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = "id")
@Table(name = "t_config", schema = "hhbot")
public class AppUserConfig {

    private static final int DEFAULT_COUNT_OF_MINUS_DAYS = 5;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "config_id", nullable = false)
    private Long id;

    @Column(name = "config_name")
    private String configName;

    @Builder.Default
    @Column(name = "experience")
    @Enumerated(EnumType.STRING)
    private ExperienceParam experience = ExperienceParam.NO_MATTER;

    @Builder.Default
    @Column(name = "last_notification_time")
    private LocalDateTime lastNotificationTime = now().minusDays(DEFAULT_COUNT_OF_MINUS_DAYS);

    @Column(name = "query_text")
    private String queryText;

    @Column(name = "area")
    private String area;

    @ManyToOne
    @JoinColumn(name = "app_user_id")
    private AppUser appUser;

    @Builder.Default
    @OneToMany(mappedBy = "config", fetch = FetchType.EAGER)
    private List<EducationLevel> educationLevels = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "config", fetch = FetchType.EAGER)
    private List<ScheduleType> scheduleTypes = new ArrayList<>();
}
