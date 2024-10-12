CREATE TABLE hhbot.t_edu_param
(
    edu_param_id BIGINT GENERATED BY DEFAULT AS IDENTITY
        CONSTRAINT edu_param_pkey PRIMARY KEY,
    param_name   VARCHAR(255),
    config_id  bigint
        CONSTRAINT fk_app_user_edu_param REFERENCES hhbot.t_config (config_id)
);