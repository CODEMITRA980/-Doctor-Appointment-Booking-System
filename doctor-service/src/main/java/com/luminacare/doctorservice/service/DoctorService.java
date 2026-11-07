package com.luminacare.doctorservice.service;

import com.luminacare.doctorservice.entity.Doctor;
import com.luminacare.doctorservice.entity.DoctorAvailability;
import com.luminacare.doctorservice.event.DoctorEventProducer;
import com.luminacare.doctorservice.repository.DoctorAvailabilityRepository;
import com.luminacare.doctorservice.repository.DoctorRepository;
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
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final DoctorAvailabilityRepository availabilityRepository;
    private final DoctorEventProducer doctorEventProducer;

    /**
     * Retrieves all doctors. Cached with TTL: 30m.
     * Expectation: Frequent reads for public listings, evicts on doctor creation/updates.
     */
    @Loggable
    @Cacheable(cacheNames = CacheNames.DOCTORS_ALL, key = "@cacheKeyBuilder.buildAll()")
    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    /**
     * Retrieves a single doctor by ID. Cached with TTL: 30m.
     * Expectation: High cache hit ratio for individual profile views.
     */
    @Loggable
    @Cacheable(cacheNames = CacheNames.DOCTORS, key = "@cacheKeyBuilder.build(#id)")
    public Optional<Doctor> getDoctorById(Long id) {
        return doctorRepository.findById(id);
    }

    @Loggable
    @Cacheable(cacheNames = CacheNames.DOCTORS_ALL, key = "@cacheKeyBuilder.buildList('spec', #specialization)")
    public List<Doctor> getDoctorsBySpecialization(String specialization) {
        return doctorRepository.findBySpecializationIgnoreCase(specialization);
    }

    @Transactional
    @RequireRole({"ADMIN"})
    @Auditable(action = "CREATE", entity = "Doctor")
    @Loggable(maskFields = {"phone"})
    @CacheEvict(cacheNames = CacheNames.DOCTORS_ALL, allEntries = true)
    public Doctor createDoctor(Doctor doctor) {
        log.info("Creating new doctor with email: {}", doctor.getEmail());
        if (doctorRepository.findByEmail(doctor.getEmail()).isPresent()) {
            throw new RuntimeException("Doctor with this email already exists");
        }
        if (doctorRepository.findByLicenseNumber(doctor.getLicenseNumber()).isPresent()) {
            throw new RuntimeException("Doctor with this license number already exists");
        }
        Doctor savedDoctor = doctorRepository.save(doctor);
        doctorEventProducer.publishDoctorCreatedEvent(savedDoctor);
        return savedDoctor;
    }

    @Transactional
    @RequireRole({"ADMIN", "DOCTOR"})
    @Auditable(action = "UPDATE", entity = "Doctor")
    @Loggable(maskFields = {"phone"})
    @Caching(put = {
        @CachePut(cacheNames = CacheNames.DOCTORS, key = "@cacheKeyBuilder.build(#id)")
    }, evict = {
        @CacheEvict(cacheNames = CacheNames.DOCTORS_ALL, allEntries = true)
    })
    public Doctor updateDoctor(Long id, Doctor updatedDoctor) {
        log.info("Updating doctor with ID: {}", id);
        return doctorRepository.findById(id)
                .map(doctor -> {
                    doctor.setFirstName(updatedDoctor.getFirstName());
                    doctor.setLastName(updatedDoctor.getLastName());
                    doctor.setSpecialization(updatedDoctor.getSpecialization());
                    doctor.setPhone(updatedDoctor.getPhone());
                    doctor.setExperienceYears(updatedDoctor.getExperienceYears());
                    doctor.setAvailability(updatedDoctor.getAvailability());
                    doctor.setConsultationFee(updatedDoctor.getConsultationFee());
                    doctor.setActive(updatedDoctor.isActive());
                    
                    Doctor saved = doctorRepository.save(doctor);
                    doctorEventProducer.publishDoctorUpdatedEvent(saved);
                    return saved;
                })
                .orElseThrow(() -> new RuntimeException("Doctor not found with id " + id));
    }

    @Loggable
    @Cacheable(cacheNames = CacheNames.SCHEDULES, key = "@cacheKeyBuilder.build(#doctorId)")
    public List<DoctorAvailability> getDoctorAvailability(Long doctorId) {
        return availabilityRepository.findByDoctorId(doctorId);
    }

    @Transactional
    @RequireRole({"ADMIN", "DOCTOR", "RECEPTIONIST"})
    @Auditable(action = "UPDATE", entity = "DoctorAvailability")
    @Loggable
    @CachePut(cacheNames = CacheNames.SCHEDULES, key = "@cacheKeyBuilder.build(#doctorId)")
    public List<DoctorAvailability> updateDoctorAvailability(Long doctorId, List<DoctorAvailability> availabilities) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new RuntimeException("Doctor not found with id " + doctorId);
        }
        
        availabilityRepository.deleteByDoctorId(doctorId);
        
        availabilities.forEach(a -> a.setDoctorId(doctorId));
        return availabilityRepository.saveAll(availabilities);
    }
}
