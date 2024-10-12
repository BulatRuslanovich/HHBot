package com.bipbup.entity;

import com.bipbup.enums.impl.ScheduleTypeParam;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
@Table(name = "t_schedule_param", schema = "hhbot")
public class ScheduleType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "edu_param_id", nullable = false)
    private Long id;

    @Column(name = "param_name")
    @Enumerated(EnumType.STRING)
    private ScheduleTypeParam param;

    @ManyToOne
    @JoinColumn(name = "config_id")
    private AppUserConfig config;
}
