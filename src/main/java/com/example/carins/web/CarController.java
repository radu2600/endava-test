package com.example.carins.web;

import com.example.carins.model.Car;
import com.example.carins.model.Claim;
import com.example.carins.model.InsurancePolicy;
import com.example.carins.service.CarService;
import com.example.carins.web.dto.CarDto;
import com.example.carins.web.dto.CarHistoryEventDto;
import com.example.carins.web.dto.ClaimDto;
import com.example.carins.web.dto.ClaimRequestDto;
import com.example.carins.web.dto.PolicyDto;
import com.example.carins.web.dto.PolicyRequestDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
public class CarController {

    private final CarService service;

    public CarController(CarService service) {
        this.service = service;
    }

    @GetMapping("/cars")
    public List<CarDto> getCars() {
        return service.listCars().stream().map(this::toDto).toList();
    }

    @GetMapping("/cars/{carId}/insurance-valid")
    public ResponseEntity<InsuranceValidityResponse> isInsuranceValid(@PathVariable Long carId, @RequestParam LocalDate date) {
        // LocalDate -> Spring automatically validates date format YYYY-MM-DD
        boolean valid = service.isInsuranceValid(carId, date);
        return ResponseEntity.ok(new InsuranceValidityResponse(carId, date.toString(), valid));
    }

    @GetMapping("/policies/{id}")
    public PolicyDto getPolicy(@PathVariable Long id) {
        return toDto(service.getPolicyById(id));
    }

    @PostMapping("/policies")
    public ResponseEntity<PolicyDto> createPolicy(@Valid @RequestBody PolicyRequestDto request) {
        InsurancePolicy createdPolicy = service.addPolicy(request);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdPolicy.getId())
                .toUri();
        return ResponseEntity.created(location).body(toDto(createdPolicy));
    }

    @PostMapping("/cars/{carId}/claims")
    public ResponseEntity<ClaimDto> registerClaim(@PathVariable Long carId, @Valid @RequestBody ClaimRequestDto claimRequest){
        Claim createdClaim = service.addClaim(carId, claimRequest);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{claimId}")
                .buildAndExpand(createdClaim.getId())
                .toUri();

        return ResponseEntity.created(location).body(toDto(createdClaim));
    }

    @GetMapping("/cars/{carId}/history")
    public List<CarHistoryEventDto> getCarHistory(@PathVariable Long carId) {
        return service.getCarHistory(carId);
    }

    private CarDto toDto(Car c) {
        var o = c.getOwner();
        return new CarDto(c.getId(), c.getVin(), c.getMake(), c.getModel(), c.getYearOfManufacture(),
                o != null ? o.getId() : null,
                o != null ? o.getName() : null,
                o != null ? o.getEmail() : null);
    }

    private ClaimDto toDto(Claim claim) {
        return new ClaimDto(claim.getId(), claim.getClaimDate(), claim.getDescription(), claim.getAmount());
    }

    private PolicyDto toDto(InsurancePolicy policy) {
        return new PolicyDto(policy.getId(), policy.getProvider(), policy.getStartDate(), policy.getEndDate());
    }

    public record InsuranceValidityResponse(Long carId, String date, boolean valid) {}
}
