package com.example.carins.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.carins.model.Car;
import com.example.carins.model.Claim;
import com.example.carins.model.InsurancePolicy;
import com.example.carins.repo.CarRepository;
import com.example.carins.repo.ClaimRepository;
import com.example.carins.repo.InsurancePolicyRepository;
import com.example.carins.web.dto.CarHistoryEventDto;
import com.example.carins.web.dto.ClaimRequestDto;
import com.example.carins.web.dto.PolicyDto;
import com.example.carins.web.dto.PolicyRequestDto;

@Service
public class CarService {

    private final CarRepository carRepository;
    private final InsurancePolicyRepository policyRepository;
    private final ClaimRepository claimRepository;

    public CarService(CarRepository carRepository, InsurancePolicyRepository policyRepository, ClaimRepository claimRepository) {
        this.carRepository = carRepository;
        this.policyRepository = policyRepository;
        this.claimRepository = claimRepository;
    }

    public List<Car> listCars() {
        return carRepository.findAll();
    }

    public boolean isInsuranceValid(Long carId, LocalDate date) {
        if (carId == null || date == null) return false;

        // Validate carId exists, return 404 if not
        if (!carRepository.existsById(carId)) {
            throw new ResourceNotFoundException("Car not found with id: " + carId);
        }
        return policyRepository.existsActiveOnDate(carId, date);
    }

    public InsurancePolicy getPolicyById(Long policyId) {
        return policyRepository.findById(policyId)
                .orElseThrow(() -> new ResourceNotFoundException("Policy not found with id: " + policyId));
    }

    public InsurancePolicy addPolicy(PolicyRequestDto request) {
        Car car = carRepository.findById(request.carId())
                .orElseThrow(() -> new ResourceNotFoundException("Cannot create policy for non-existent car with id: " + request.carId()));

        InsurancePolicy policy = new InsurancePolicy();
        policy.setCar(car);
        policy.setProvider(request.provider());
        policy.setStartDate(request.startDate());
        policy.setEndDate(request.endDate());

        return policyRepository.save(policy);
    }

    public Claim addClaim(Long carId, ClaimRequestDto request) {
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new ResourceNotFoundException("Car not found with id: " + carId));

        if (!isInsuranceValid(carId, request.claimDate())) {
            throw new InvalidRequestException("Car with id " + carId + " does not have a valid insurance policy on " + request.claimDate());
        }

        Claim claim = new Claim();
        claim.setCar(car);
        claim.setClaimDate(request.claimDate());
        claim.setDescription(request.description());
        claim.setAmount(request.amount());

        return claimRepository.save(claim);
    }

    public List<CarHistoryEventDto> getCarHistory(Long carId) {
        if (!carRepository.existsById(carId)) {
            throw new ResourceNotFoundException("Car not found with id: " + carId);
        }

        List<CarHistoryEventDto> historyEvents = new ArrayList<>();

        policyRepository.findByCarId(carId).forEach(policy -> {
            PolicyDto policyDetails = new PolicyDto(policy.getId(), policy.getProvider(), policy.getStartDate(), policy.getEndDate());
            historyEvents.add(new CarHistoryEventDto(policy.getStartDate(), "INSURANCE_POLICY_START", policyDetails));
        });

        claimRepository.findByCarId(carId).forEach(claim -> {
            com.example.carins.web.dto.ClaimDto claimDetails = new com.example.carins.web.dto.ClaimDto(claim.getId(), claim.getClaimDate(), claim.getDescription(), claim.getAmount());
            historyEvents.add(new CarHistoryEventDto(claim.getClaimDate(), "CLAIM_REGISTERED", claimDetails));
        });

        // Sort events chronologically
        historyEvents.sort(Comparator.comparing(CarHistoryEventDto::eventDate));

        return historyEvents;
    }
}
