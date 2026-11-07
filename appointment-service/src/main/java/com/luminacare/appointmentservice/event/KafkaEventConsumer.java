package com.luminacare.appointmentservice.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class KafkaEventConsumer {

    @KafkaListener(topics = "patient-events", groupId = "appointment-group")
    public void consumePatientEvents(Object patientData) {
        log.info("Received patient event in appointment-service: {}", patientData);
        // Here we could update a denormalized patient table if needed for fast reads
    }

    @KafkaListener(topics = "doctor-events", groupId = "appointment-group")
    public void consumeDoctorEvents(Object doctorData) {
        log.info("Received doctor event in appointment-service: {}", doctorData);
        // Here we could update a denormalized doctor table if needed for fast reads
    }
}
