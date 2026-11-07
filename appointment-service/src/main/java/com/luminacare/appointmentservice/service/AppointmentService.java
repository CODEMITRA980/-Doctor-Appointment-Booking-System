package com.luminacare.appointmentservice.service;

import com.luminacare.appointmentservice.client.DoctorClient;
import com.luminacare.appointmentservice.client.PatientClient;
import com.luminacare.appointmentservice.entity.Appointment;
import com.luminacare.appointmentservice.event.AppointmentEventProducer;
import com.luminacare.appointmentservice.repository.AppointmentRepository;
import com.luminacare.common.enums.AppointmentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import com.luminacare.common.constants.CacheNames;
import com.luminacare.common.annotation.Auditable;
import com.luminacare.common.annotation.RequireRole;
import com.luminacare.common.annotation.Loggable;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentEventProducer eventProducer;
    private final PatientClient patientClient;
    private final DoctorClient doctorClient;

    /**
     * Retrieves an appointment by ID. Cached with TTL: 5m.
     * Expectation: Short-lived cache due to frequent status changes.
     */
    @Loggable
    @RequireRole({"ADMIN", "DOCTOR", "RECEPTIONIST", "PATIENT"})
    @Cacheable(cacheNames = CacheNames.APPOINTMENTS, key = "@cacheKeyBuilder.build(#id)")
    public Optional<Appointment> getAppointmentById(Long id) {
        return appointmentRepository.findById(id);
    }

    @Loggable
    @RequireRole({"ADMIN", "DOCTOR", "RECEPTIONIST", "PATIENT"})
    @Cacheable(cacheNames = CacheNames.APPOINTMENTS, key = "@cacheKeyBuilder.buildList('patient', #patientId)")
    public List<Appointment> getAppointmentsByPatientId(Long patientId) {
        return appointmentRepository.findByPatientId(patientId);
    }

    @Loggable
    @RequireRole({"ADMIN", "DOCTOR", "RECEPTIONIST"})
    @Cacheable(cacheNames = CacheNames.APPOINTMENTS, key = "@cacheKeyBuilder.buildList('doctor', #doctorId)")
    public List<Appointment> getAppointmentsByDoctorId(Long doctorId) {
        return appointmentRepository.findByDoctorId(doctorId);
    }

    @Transactional
    @RequireRole({"ADMIN", "RECEPTIONIST", "PATIENT"})
    @Auditable(action = "CREATE", entity = "Appointment")
    @Loggable
    @CacheEvict(cacheNames = CacheNames.APPOINTMENTS, allEntries = true)
    public Appointment createAppointment(Appointment appointment) {
        log.info("Creating appointment for patient {} with doctor {}", appointment.getPatientId(), appointment.getDoctorId());
        
        // 1. Validate patient existence
        try {
            patientClient.getPatientById(appointment.getPatientId());
        } catch (Exception e) {
            throw new RuntimeException("Invalid patient ID: " + appointment.getPatientId());
        }

        // 2. Validate doctor existence
        try {
            doctorClient.getDoctorById(appointment.getDoctorId());
        } catch (Exception e) {
            throw new RuntimeException("Invalid doctor ID: " + appointment.getDoctorId());
        }
        
        // 3. No double booking
        boolean isDoubleBooked = appointmentRepository.findByDoctorIdAndAppointmentDateAndSlot(
                appointment.getDoctorId(), 
                appointment.getAppointmentDate(), 
                appointment.getSlot()
        ).isPresent();

        if (isDoubleBooked) {
            throw new RuntimeException("Doctor is already booked for this slot");
        }

        appointment.setStatus(AppointmentStatus.SCHEDULED);
        Appointment savedAppointment = appointmentRepository.save(appointment);
        eventProducer.publishAppointmentCreatedEvent(savedAppointment);
        
        return savedAppointment;
    }

    @Transactional
    @RequireRole({"ADMIN", "RECEPTIONIST", "PATIENT", "DOCTOR"})
    @Auditable(action = "CANCEL", entity = "Appointment")
    @Loggable
    @Caching(put = {
        @CachePut(cacheNames = CacheNames.APPOINTMENTS, key = "@cacheKeyBuilder.build(#id)")
    }, evict = {
        @CacheEvict(cacheNames = CacheNames.APPOINTMENTS, key = "@cacheKeyBuilder.buildList('patient', result.patientId)"),
        @CacheEvict(cacheNames = CacheNames.APPOINTMENTS, key = "@cacheKeyBuilder.buildList('doctor', result.doctorId)")
    })
    public Appointment cancelAppointment(Long id) {
        log.info("Canceling appointment {}", id);
        return appointmentRepository.findById(id).map(appointment -> {
            appointment.setStatus(AppointmentStatus.CANCELLED);
            Appointment saved = appointmentRepository.save(appointment);
            eventProducer.publishAppointmentUpdatedEvent(saved);
            return saved;
        }).orElseThrow(() -> new RuntimeException("Appointment not found"));
    }

    @Transactional
    @RequireRole({"ADMIN", "RECEPTIONIST", "PATIENT"})
    @Auditable(action = "RESCHEDULE", entity = "Appointment")
    @Loggable
    @Caching(put = {
        @CachePut(cacheNames = CacheNames.APPOINTMENTS, key = "@cacheKeyBuilder.build(#id)")
    }, evict = {
        @CacheEvict(cacheNames = CacheNames.APPOINTMENTS, key = "@cacheKeyBuilder.buildList('patient', result.patientId)"),
        @CacheEvict(cacheNames = CacheNames.APPOINTMENTS, key = "@cacheKeyBuilder.buildList('doctor', result.doctorId)")
    })
    public Appointment rescheduleAppointment(Long id, Appointment rescheduledDetails) {
        log.info("Rescheduling appointment {}", id);
        return appointmentRepository.findById(id).map(appointment -> {
            // Check double booking for new slot
            boolean isDoubleBooked = appointmentRepository.findByDoctorIdAndAppointmentDateAndSlot(
                    appointment.getDoctorId(), 
                    rescheduledDetails.getAppointmentDate(), 
                    rescheduledDetails.getSlot()
            ).isPresent();

            if (isDoubleBooked) {
                throw new RuntimeException("Doctor is already booked for this new slot");
            }

            appointment.setAppointmentDate(rescheduledDetails.getAppointmentDate());
            appointment.setSlot(rescheduledDetails.getSlot());
            appointment.setStatus(AppointmentStatus.RESCHEDULED);
            
            Appointment saved = appointmentRepository.save(appointment);
            eventProducer.publishAppointmentUpdatedEvent(saved);
            return saved;
        }).orElseThrow(() -> new RuntimeException("Appointment not found"));
    }
}
