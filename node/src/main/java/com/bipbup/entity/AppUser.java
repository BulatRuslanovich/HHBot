package com.bipbup.entity;

import com.bipbup.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "app_user")
@EqualsAndHashCode(exclude = "userId")
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
    private Role role;

    @OneToMany
    @JoinColumn(name = "app_user_id", referencedColumnName = "userId")
    private List<AppUserConfig> appUserConfigs;
}
