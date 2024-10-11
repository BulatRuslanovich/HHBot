package com.bipbup.entity;

import com.bipbup.enums.impl.EducationLevelParam;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(exclude = "id")
@Table(name = "t_edu_param", schema = "hhbot")
public class EducationLevelParamEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "edu_param_id", nullable = false)
    private Long id;

    @Size(max = 255)
    @Column(name = "param_name")
    @Enumerated(EnumType.STRING)
    private EducationLevelParam paramName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "config_id")
    private AppUserConfig config;
}