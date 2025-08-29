package com.example.carins.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CarHistoryEventDto(
    LocalDate eventDate,
    String eventType,
    Object details
) {}
