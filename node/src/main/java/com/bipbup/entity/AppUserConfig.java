package com.bipbup.entity;

import com.bipbup.enums.impl.ExperienceParam;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.time.LocalDateTime.now;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(exclude = "id")
@Table(name = "t_config", schema = "hhbot")
public class AppUserConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "config_id", nullable = false)
    private Long id;

    @Size(max = 255)
    @Column(name = "config_name")
    private String configName;

    @Size(max = 255)
    @Builder.Default
    @Column(name = "experience")
    @Enumerated(EnumType.STRING)
    private ExperienceParam experience = ExperienceParam.NO_MATTER;;

    @Builder.Default
    @Column(name = "last_notification_time")
    private LocalDateTime lastNotificationTime = now().minusDays(5);

    @Size(max = 255)
    @Column(name = "query_text")
    private String queryText;

    @Size(max = 255)
    @Column(name = "area")
    private String area;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "app_user_id")
    private AppUser appUser;

    @OneToMany(mappedBy = "config")
    private List<EducationLevelParamEntity> eduParams = new ArrayList<>();

    @OneToMany(mappedBy = "config")
    private List<ScheduleParamEntity> scheduleParams = new ArrayList<>();
}