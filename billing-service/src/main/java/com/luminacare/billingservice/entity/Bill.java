package com.luminacare.billingservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bills")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Appointment ID is mandatory")
    @Column(unique = true)
    private Long appointmentId;

    @NotNull(message = "Patient ID is mandatory")
    private Long patientId;

    @NotNull(message = "Amount is mandatory")
    private BigDecimal amount;

    private BigDecimal tax;

    @NotNull(message = "Total is mandatory")
    private BigDecimal total;

    private String status; // PENDING, PAID, CANCELLED

    private String paymentMethod; // CASH, CARD, ONLINE

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime generatedAt;

    private LocalDateTime paidAt;
}
