package com.luminacare.doctorservice.event;

import com.luminacare.doctorservice.entity.Doctor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DoctorEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "doctor-events";

    public void publishDoctorCreatedEvent(Doctor doctor) {
        try {
            log.info("Publishing doctor created event for doctor ID: {}", doctor.getId());
            kafkaTemplate.send(TOPIC, "doctor.created", doctor);
        } catch (Exception e) {
            log.warn("Failed to publish doctor created event to Kafka: {}", e.getMessage());
        }
    }

    public void publishDoctorUpdatedEvent(Doctor doctor) {
        try {
            log.info("Publishing doctor updated event for doctor ID: {}", doctor.getId());
            kafkaTemplate.send(TOPIC, "doctor.updated", doctor);
        } catch (Exception e) {
            log.warn("Failed to publish doctor updated event to Kafka: {}", e.getMessage());
        }
    }
}
