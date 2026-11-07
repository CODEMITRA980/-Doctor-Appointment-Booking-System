package com.luminacare.patientservice.service;

import com.luminacare.patientservice.entity.Patient;
import com.luminacare.patientservice.event.PatientEventProducer;
import com.luminacare.patientservice.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import com.luminacare.common.exception.BusinessException;
import com.luminacare.common.exception.ResourceNotFoundException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import com.luminacare.common.constants.CacheNames;
import com.luminacare.common.annotation.Auditable;
import com.luminacare.common.annotation.RequireRole;
import com.luminacare.common.annotation.Loggable;

@Service
@RequiredArgsConstructor
@Slf4j
public class PatientService {

    private final PatientRepository patientRepository;
    private final PatientEventProducer patientEventProducer;

    /**
     * Retrieves all patients. Cached with TTL: 30m.
     * Expectation: Frequent read hits for patient listings, evicts when a new patient is created.
     */
    @RequireRole({"ADMIN", "DOCTOR", "RECEPTIONIST"})
    @Loggable
    @Cacheable(cacheNames = CacheNames.PATIENTS, key = "@cacheKeyBuilder.buildAll()")
    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    /**
     * Retrieves a patient by ID. Cached with TTL: 30m.
     * Expectation: High cache hit ratio for individual patient views.
     */
    @RequireRole({"ADMIN", "DOCTOR", "RECEPTIONIST", "PATIENT"})
    @Loggable
    @Cacheable(cacheNames = CacheNames.PATIENTS, key = "@cacheKeyBuilder.build(#id)")
    public Optional<Patient> getPatientById(Long id) {
        return patientRepository.findById(id);
    }

    @Transactional
    @RequireRole({"ADMIN", "RECEPTIONIST"})
    @Auditable(action = "CREATE", entity = "Patient")
    @Loggable(maskFields = {"phone", "address"})
    @CacheEvict(cacheNames = CacheNames.PATIENTS, allEntries = true)
    public Patient createPatient(Patient patient) {
        log.info("Creating new patient with email: {}", patient.getEmail());
        if (patient.getEmail() != null && patientRepository.findByEmail(patient.getEmail()).isPresent()) {
            throw new BusinessException("Patient with email " + patient.getEmail() + " already exists");
        }
        Patient savedPatient = patientRepository.save(patient);
        patientEventProducer.publishPatientCreatedEvent(savedPatient);
        return savedPatient;
    }

    @Transactional
    @RequireRole({"ADMIN", "RECEPTIONIST", "PATIENT"})
    @Auditable(action = "UPDATE", entity = "Patient")
    @Loggable(maskFields = {"phone", "address"})
    @Caching(evict = {
        @CacheEvict(cacheNames = CacheNames.PATIENTS, key = "@cacheKeyBuilder.build(#id)"),
        @CacheEvict(cacheNames = CacheNames.PATIENTS, key = "@cacheKeyBuilder.buildAll()"),
        @CacheEvict(cacheNames = CacheNames.PATIENTS, allEntries = true)
    })
    public Patient updatePatient(Long id, Patient updatedPatient) {
        log.info("Updating patient with ID: {}", id);
        return patientRepository.findById(id)
                .map(patient -> {
                    if (updatedPatient.getFirstName() != null) {
                        patient.setFirstName(updatedPatient.getFirstName());
                    }
                    if (updatedPatient.getLastName() != null) {
                        patient.setLastName(updatedPatient.getLastName());
                    }
                    if (updatedPatient.getEmail() != null && !updatedPatient.getEmail().equalsIgnoreCase(patient.getEmail())) {
                        if (patientRepository.findByEmail(updatedPatient.getEmail()).isPresent()) {
                            throw new BusinessException("Patient with email " + updatedPatient.getEmail() + " already exists");
                        }
                        patient.setEmail(updatedPatient.getEmail());
                    }
                    if (updatedPatient.getPhone() != null) {
                        patient.setPhone(updatedPatient.getPhone());
                    }
                    if (updatedPatient.getDob() != null) {
                        patient.setDob(updatedPatient.getDob());
                    }
                    if (updatedPatient.getGender() != null) {
                        patient.setGender(updatedPatient.getGender());
                    }
                    if (updatedPatient.getAddress() != null) {
                        patient.setAddress(updatedPatient.getAddress());
                    }
                    if (updatedPatient.getBloodGroup() != null) {
                        patient.setBloodGroup(updatedPatient.getBloodGroup());
                    }
                    if (updatedPatient.getEmergencyContact() != null) {
                        patient.setEmergencyContact(updatedPatient.getEmergencyContact());
                    }
                    
                    Patient saved = patientRepository.save(patient);
                    patientEventProducer.publishPatientUpdatedEvent(saved);
                    return saved;
                })
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "id", id));
    }

    @Transactional
    @RequireRole({"ADMIN"})
    @Auditable(action = "DELETE", entity = "Patient")
    @Loggable
    @Caching(evict = {
        @CacheEvict(cacheNames = CacheNames.PATIENTS, key = "@cacheKeyBuilder.build(#id)"),
        @CacheEvict(cacheNames = CacheNames.PATIENTS, key = "@cacheKeyBuilder.buildAll()"),
        @CacheEvict(cacheNames = CacheNames.PATIENTS, allEntries = true)
    })
    public void deletePatient(Long id) {
        log.info("Deleting patient with ID: {}", id);
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "id", id));
        patientRepository.delete(patient);
    }

    @RequireRole({"ADMIN", "DOCTOR", "RECEPTIONIST"})
    @Loggable
    @Cacheable(cacheNames = CacheNames.PATIENTS, key = "@cacheKeyBuilder.buildList('search', #query)")
    public List<Patient> searchPatients(String query) {
        return patientRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(query, query);
    }
}
