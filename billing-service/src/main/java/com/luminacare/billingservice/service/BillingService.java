package com.luminacare.billingservice.service;

import com.luminacare.billingservice.entity.Bill;
import com.luminacare.billingservice.repository.BillRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BillingService {

    private final BillRepository billRepository;

    public Optional<Bill> getBillById(Long id) {
        return billRepository.findById(id);
    }

    public List<Bill> getBillsByPatientId(Long patientId) {
        return billRepository.findByPatientId(patientId);
    }

    @Transactional
    public Bill generateBillForAppointment(Map<String, Object> appointmentData) {
        Long appointmentId = ((Number) appointmentData.get("id")).longValue();
        
        if (billRepository.findByAppointmentId(appointmentId).isPresent()) {
            log.info("Bill already exists for appointment {}", appointmentId);
            return billRepository.findByAppointmentId(appointmentId).get();
        }
        
        Long patientId = ((Number) appointmentData.get("patientId")).longValue();
        
        // Mocking fee calculation, in a real scenario you would fetch Doctor's fee or a fixed procedure fee.
        BigDecimal amount = new BigDecimal("500.00"); 
        BigDecimal tax = amount.multiply(new BigDecimal("0.18")).setScale(2, RoundingMode.HALF_UP); // 18% tax
        BigDecimal total = amount.add(tax);

        Bill bill = Bill.builder()
                .appointmentId(appointmentId)
                .patientId(patientId)
                .amount(amount)
                .tax(tax)
                .total(total)
                .status("PENDING")
                .build();

        log.info("Generating bill for appointment {}", appointmentId);
        return billRepository.save(bill);
    }

    @Transactional
    public Bill payBill(Long id, String paymentMethod) {
        log.info("Paying bill {} using {}", id, paymentMethod);
        return billRepository.findById(id).map(bill -> {
            if ("PAID".equals(bill.getStatus())) {
                throw new RuntimeException("Bill is already paid");
            }
            bill.setStatus("PAID");
            bill.setPaymentMethod(paymentMethod);
            bill.setPaidAt(LocalDateTime.now());
            return billRepository.save(bill);
        }).orElseThrow(() -> new RuntimeException("Bill not found"));
    }

    public List<Bill> getDailyReport() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        return billRepository.findBillsGeneratedToday(startOfDay, endOfDay);
    }
}
