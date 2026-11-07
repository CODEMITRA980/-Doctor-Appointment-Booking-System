package com.luminacare.billingservice.event;

import com.luminacare.billingservice.service.BillingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class AppointmentEventConsumer {

    private final BillingService billingService;

    @KafkaListener(topics = "appointment-events", groupId = "billing-group")
    public void consumeAppointmentEvents(Map<String, Object> appointmentData) {
        log.info("Received appointment event in billing-service: {}", appointmentData);
        // Assuming we auto-generate the bill as soon as appointment is created for simplicity,
        // or check status if it's "COMPLETED". For now, we generate on creation/update if missing.
        try {
            billingService.generateBillForAppointment(appointmentData);
        } catch (Exception e) {
            log.error("Failed to generate bill for appointment: {}", e.getMessage());
        }
    }
}
