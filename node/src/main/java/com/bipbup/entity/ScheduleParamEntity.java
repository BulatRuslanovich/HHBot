package com.bipbup.entity;

import com.bipbup.enums.impl.ScheduleTypeParam;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(exclude = "configId")
@Table(name = "t_schedule_param", schema = "hhbot")
public class ScheduleParamEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "edu_param_id", nullable = false)
    private Long id;

    @Size(max = 255)
    @Column(name = "param_name")
    @Enumerated(EnumType.STRING)
    private ScheduleTypeParam paramName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "config_id")
    private AppUserConfig config;
}