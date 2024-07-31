package com.bipbup.entity;

import com.bipbup.enums.AppUserState;
import jakarta.persistence.*;
import lombok.*;
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

    private Long telegramId;

    @CreationTimestamp
    @Builder.Default
    private LocalDateTime firstLoginDate = LocalDateTime.now();

    private String username;
    private String firstName;
    private String lastName;


    @Enumerated(EnumType.STRING)
    private AppUserState state;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "appUser", fetch = FetchType.EAGER)
    private List<AppUserConfig> appUserConfigs;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o) || o.getClass() != this.getClass())
            return false;
        AppUser appUser = (AppUser) o;
        return telegramId != null && Objects.equals(telegramId, appUser.telegramId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
