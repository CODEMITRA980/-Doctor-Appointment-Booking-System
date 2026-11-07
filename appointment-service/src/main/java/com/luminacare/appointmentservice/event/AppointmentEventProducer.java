package com.luminacare.appointmentservice.event;

import com.luminacare.appointmentservice.entity.Appointment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AppointmentEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "appointment-events";

    public void publishAppointmentCreatedEvent(Appointment appointment) {
        try {
            log.info("Publishing appointment created event for ID: {}", appointment.getId());
            kafkaTemplate.send(TOPIC, "appointment.created", appointment);
        } catch (Exception e) {
            log.warn("Failed to publish appointment created event to Kafka: {}", e.getMessage());
        }
    }

    public void publishAppointmentUpdatedEvent(Appointment appointment) {
        try {
            log.info("Publishing appointment updated event for ID: {}", appointment.getId());
            kafkaTemplate.send(TOPIC, "appointment.updated", appointment);
        } catch (Exception e) {
            log.warn("Failed to publish appointment updated event to Kafka: {}", e.getMessage());
        }
    }
}
