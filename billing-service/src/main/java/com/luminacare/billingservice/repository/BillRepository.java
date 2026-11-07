package com.luminacare.billingservice.repository;

import com.luminacare.billingservice.entity.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {
    List<Bill> findByPatientId(Long patientId);
    Optional<Bill> findByAppointmentId(Long appointmentId);
    
    @Query("SELECT b FROM Bill b WHERE b.generatedAt >= :startOfDay AND b.generatedAt < :endOfDay")
    List<Bill> findBillsGeneratedToday(LocalDateTime startOfDay, LocalDateTime endOfDay);
}
