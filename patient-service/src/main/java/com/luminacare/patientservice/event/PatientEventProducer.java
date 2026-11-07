package com.luminacare.patientservice.event;

import com.luminacare.patientservice.entity.Patient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PatientEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "patient-events";

    public void publishPatientCreatedEvent(Patient patient) {
        try {
            log.info("Publishing patient created event for patient ID: {}", patient.getId());
            kafkaTemplate.send(TOPIC, "patient.created", patient);
        } catch (Exception e) {
            log.warn("Failed to publish patient created event to Kafka: {}", e.getMessage());
        }
    }

    public void publishPatientUpdatedEvent(Patient patient) {
        try {
            log.info("Publishing patient updated event for patient ID: {}", patient.getId());
            kafkaTemplate.send(TOPIC, "patient.updated", patient);
        } catch (Exception e) {
            log.warn("Failed to publish patient updated event to Kafka: {}", e.getMessage());
        }
    }
}
