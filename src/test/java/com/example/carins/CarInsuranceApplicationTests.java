package com.example.carins;

import com.example.carins.web.dto.ClaimRequestDto;
import com.example.carins.web.dto.PolicyRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CarInsuranceApplicationTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

     @Test
    void A_createPolicy_shouldSucceed_whenRequestIsValid() throws Exception {
        PolicyRequestDto request = new PolicyRequestDto(
                1L, 
                "Test Provider",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2027, 1, 1) // endDate is present
        );

        mockMvc.perform(post("/api/policies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.provider", is("Test Provider")))
                .andExpect(jsonPath("$.endDate", is("2027-01-01")));
    }

    @Test
    void A_createPolicy_shouldFail_whenEndDateIsMissing() throws Exception {
        // JSON  to simulate a request where the endDate field is missing
        String invalidRequestJson = """
        {
            "carId": 1,
            "provider": "Incomplete Provider",
            "startDate": "2026-01-01"
        }
        """;

        mockMvc.perform(post("/api/policies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void B_registerClaim_shouldReturn201_whenRequestIsValidAndInsuranceExists() throws Exception {
        Long carId = 1L;
        ClaimRequestDto request = new ClaimRequestDto(LocalDate.of(2024, 8, 28), "Damage to bumper", new BigDecimal("1200.50"));

        mockMvc.perform(post("/api/cars/{carId}/claims", carId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.description", is("Damage to bumper")))
                .andExpect(jsonPath("$.amount", is(1200.50)));
    }

    @Test
    void B_registerClaim_shouldReturn400_whenInsuranceIsInvalidForClaimDate() throws Exception {
        Long carId = 1L;
        // This claim date is outside any policy for car 1
        ClaimRequestDto request = new ClaimRequestDto(LocalDate.of(2023, 12, 31), "Old damage", new BigDecimal("500.00"));

        mockMvc.perform(post("/api/cars/{carId}/claims", carId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void B_getCarHistory_shouldReturnSortedEvents() throws Exception {
        Long carId = 1L;
        ClaimRequestDto request = new ClaimRequestDto(LocalDate.of(2024, 6, 1), "Scratched paint", new BigDecimal("300"));
        mockMvc.perform(post("/api/cars/{carId}/claims", carId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // When getting history
        mockMvc.perform(get("/api/cars/{carId}/history", carId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3))) // 2 policies from import.sql + 1 new claim
                .andExpect(jsonPath("$[0].eventDate", is("2024-01-01")))
                .andExpect(jsonPath("$[1].eventDate", is("2024-06-01")))
                .andExpect(jsonPath("$[2].eventDate", is("2025-01-01")));
    }

    @Test
    void B_getCarHistory_shouldReturn404_whenCarNotFound() throws Exception {
        mockMvc.perform(get("/api/cars/999/history"))
                .andExpect(status().isNotFound());
    }


    @Test
    void C_isInsuranceValid_shouldReturn404_whenCarNotFound() throws Exception {
        mockMvc.perform(get("/api/cars/999/insurance-valid?date=2025-01-01"))
                .andExpect(status().isNotFound());
    }

    @Test
    void C_isInsuranceValid_shouldReturn400_whenDateFormatIsInvalid() throws Exception {
        mockMvc.perform(get("/api/cars/1/insurance-valid?date=not-a-date"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void C_isInsuranceValid_shouldReturn400_whenDateIsImpossible() throws Exception {
        // An impossible date: Feb 30th
        mockMvc.perform(get("/api/cars/1/insurance-valid?date=2025-02-30"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void C_isInsuranceValid_shouldReturn200_whenRequestIsValid() throws Exception {
        mockMvc.perform(get("/api/cars/1/insurance-valid?date=2024-06-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.carId", is(1)))
                .andExpect(jsonPath("$.date", is("2024-06-01")))
                .andExpect(jsonPath("$.valid", is(true)));
    }
}
