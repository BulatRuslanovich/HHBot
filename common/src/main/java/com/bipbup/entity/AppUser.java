package com.bipbup.entity;

import com.bipbup.enums.AppUserState;
import jakarta.persistence.*;
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

    @OneToMany(mappedBy = "appUser", cascade = CascadeType.ALL,  fetch = FetchType.LAZY)
    private List<AppUserConfig> appUserConfigs;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null
                || Hibernate.getClass(this) != Hibernate.getClass(o)
                || o.getClass() != this.getClass())
            return false;
        AppUser appUser = (AppUser) o;
        return telegramId != null && Objects.equals(telegramId, appUser.telegramId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
