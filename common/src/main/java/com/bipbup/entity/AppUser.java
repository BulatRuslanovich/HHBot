package com.bipbup.entity;

import com.bipbup.enums.AppUserState;
import com.bipbup.enums.ExperienceParam;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "app_user")
@Entity
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long telegramId;

    @CreationTimestamp
    @Builder.Default
    private LocalDateTime firstLoginDate = LocalDateTime.now();

    private String username;
    private String firstName;
    private String lastName;

    @Builder.Default
    private LocalDateTime lastNotificationTime = LocalDateTime.now().minusDays(1);

    @Enumerated(EnumType.STRING)
    private AppUserState state;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private ExperienceParam experience = ExperienceParam.NO_MATTER;

    private String queryText;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null|| Hibernate.getClass(this) != Hibernate.getClass(o) || o.getClass() != this.getClass()) return false;
        AppUser appUser = (AppUser) o;
        return telegramId != null && Objects.equals(telegramId, appUser.telegramId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
