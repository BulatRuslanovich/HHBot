package com.bipbup.entity;

import com.bipbup.enums.AppUserState;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;
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
    private Long userId;

    @Column(unique = true, nullable = false)
    private Long telegramId;

    @CreationTimestamp
    private LocalDateTime firstLoginDate;

    private String username;

    private String firstName;

    private String lastName;

    @Enumerated(EnumType.STRING)
    private AppUserState state;

    @OneToMany
    @JoinColumn(name = "app_user_id", referencedColumnName = "userId")
    private List<AppUserConfig> appUserConfigs;

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null
                || Hibernate.getClass(this) != Hibernate.getClass(o)
                || o.getClass() != this.getClass()) {
            return false;
        }
        AppUser appUser = (AppUser) o;
        return telegramId != null
                && Objects.equals(telegramId, appUser.telegramId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
