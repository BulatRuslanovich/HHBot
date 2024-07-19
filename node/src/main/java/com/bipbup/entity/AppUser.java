package com.bipbup.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.Hibernate;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "app_user")
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long telegramId;

    @CreationTimestamp
    private LocalDateTime firstLoginDate;
    private String username;
    private String firstName;
    private String lastName;
    private LocalDateTime lastNotificationTime;
    private String email;
    private Boolean isActive;

//    TODO: Bulat, it is big shit, redo it
//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if ((o == null) || (Hibernate.getClass(this) != Hibernate.getClass(o)))
//            return false;
//        AppUser appUser = (AppUser) o;
//        return telegramId != null && Objects.equals(telegramId, appUser.telegramId);
//    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
