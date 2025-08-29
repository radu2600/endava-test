package com.example.carins.service;

import com.example.carins.model.InsurancePolicy;
import com.example.carins.repo.InsurancePolicyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
public class PolicyExpirationNotifier {

    private static final Logger log = LoggerFactory.getLogger(PolicyExpirationNotifier.class);
    private final InsurancePolicyRepository policyRepository;

    public PolicyExpirationNotifier(InsurancePolicyRepository policyRepository) {
        this.policyRepository = policyRepository;
    }

    @Scheduled(cron = "0 0 * * * ?") // Runs hourly
    @Transactional
    public void logExpiredPolicies() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        log.info("Checking for policies that expired on {}", yesterday);

        List<InsurancePolicy> expiredPolicies = policyRepository.findByEndDateAndExpirationNotifiedIsFalse(yesterday);

        if (expiredPolicies.isEmpty()) {
            return;
        }

        for (InsurancePolicy policy : expiredPolicies) {
            log.warn("Policy {} for car {} expired on {}", policy.getId(), policy.getCar().getId(), policy.getEndDate());
            policy.setExpirationNotified(true);
        }

        policyRepository.saveAll(expiredPolicies);
        log.info("Processed and flagged {} expired policies, notifying car owners.", expiredPolicies.size());
    }
}
