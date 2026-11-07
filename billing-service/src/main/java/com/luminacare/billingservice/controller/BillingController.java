package com.luminacare.billingservice.controller;

import com.luminacare.billingservice.entity.Bill;
import com.luminacare.billingservice.service.BillingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/billing")
@RequiredArgsConstructor
@Tag(name = "Billing Controller", description = "Endpoints for managing bills")
public class BillingController {

    private final BillingService billingService;

    @GetMapping("/{id}")
    @Operation(summary = "Get bill by ID")
    public ResponseEntity<Bill> getBillById(@PathVariable Long id) {
        return billingService.getBillById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/patient/{patientId}")
    @Operation(summary = "Get all bills for a patient")
    public ResponseEntity<List<Bill>> getBillsByPatientId(@PathVariable Long patientId) {
        return ResponseEntity.ok(billingService.getBillsByPatientId(patientId));
    }

    @PostMapping("/{id}/pay")
    @Operation(summary = "Pay a bill")
    public ResponseEntity<Bill> payBill(@PathVariable Long id, @RequestParam String paymentMethod) {
        return ResponseEntity.ok(billingService.payBill(id, paymentMethod));
    }

    @GetMapping("/reports/daily")
    @Operation(summary = "Get daily billing report")
    public ResponseEntity<List<Bill>> getDailyReport() {
        return ResponseEntity.ok(billingService.getDailyReport());
    }
}
