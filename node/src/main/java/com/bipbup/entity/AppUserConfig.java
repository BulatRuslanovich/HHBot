package com.bipbup.entity;

import com.bipbup.enums.impl.ExperienceParam;
import jakarta.persistence.CascadeType;
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

    @Column(name = "experience")
    @Enumerated(EnumType.STRING)
    private ExperienceParam experience;

    @Column(name = "last_notification_time")
    private LocalDateTime lastNotificationTime;

    @Column(name = "query_text")
    private String queryText;

    @Column(name = "area")
    private String area;

    @ManyToOne
    @JoinColumn(name = "app_user_id")
    private AppUser appUser;

    @OneToMany(mappedBy = "config", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<EducationLevel> educationLevels;

    @OneToMany(mappedBy = "config", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<ScheduleType> scheduleTypes;
}
