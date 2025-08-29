package com.example.carins.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ClaimRequestDto(
    @NotNull(message = "Claim date cannot be null.")
    LocalDate claimDate,

    @NotBlank(message = "Description cannot be blank.")
    @Size(max = 255, message = "Description must be less than 255 characters.")
    String description,

    @NotNull(message = "Amount cannot be null.")
    @Positive(message = "Amount must be positive.")
    BigDecimal amount
) {}
