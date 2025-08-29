package com.example.carins.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record PolicyRequestDto(
    @NotNull(message = "Car ID cannot be null.")
    Long carId,

    @NotBlank(message = "Provider cannot be blank.")
    String provider,

    @NotNull(message = "Start date cannot be null.")
    LocalDate startDate,

    @NotNull(message = "End date cannot be null.") 
    LocalDate endDate
) {}
