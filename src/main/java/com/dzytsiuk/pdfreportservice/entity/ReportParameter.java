package com.dzytsiuk.pdfreportservice.entity;

import com.dzytsiuk.pdfreportservice.config.LocalDateTimeDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReportParameter {
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime fromDate;
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime toDate;
}
