package com.luminacare.doctorservice.controller;

import com.luminacare.doctorservice.entity.Doctor;
import com.luminacare.doctorservice.entity.DoctorAvailability;
import com.luminacare.doctorservice.service.DoctorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
@Tag(name = "Doctor Controller", description = "Endpoints for managing doctors and their availability")
public class DoctorController {

    private final DoctorService doctorService;

    @GetMapping
    @Operation(summary = "Get all doctors")
    public ResponseEntity<List<Doctor>> getAllDoctors() {
        return ResponseEntity.ok(doctorService.getAllDoctors());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get doctor by ID")
    public ResponseEntity<Doctor> getDoctorById(@PathVariable Long id) {
        return doctorService.getDoctorById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Create a new doctor")
    public ResponseEntity<Doctor> createDoctor(@Valid @RequestBody Doctor doctor) {
        return new ResponseEntity<>(doctorService.createDoctor(doctor), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing doctor")
    public ResponseEntity<Doctor> updateDoctor(@PathVariable Long id, @Valid @RequestBody Doctor doctor) {
        return ResponseEntity.ok(doctorService.updateDoctor(id, doctor));
    }

    @GetMapping("/specialization/{spec}")
    @Operation(summary = "Get doctors by specialization")
    public ResponseEntity<List<Doctor>> getDoctorsBySpecialization(@PathVariable String spec) {
        return ResponseEntity.ok(doctorService.getDoctorsBySpecialization(spec));
    }

    @GetMapping("/{id}/availability")
    @Operation(summary = "Get doctor availability")
    public ResponseEntity<List<DoctorAvailability>> getDoctorAvailability(@PathVariable Long id) {
        return ResponseEntity.ok(doctorService.getDoctorAvailability(id));
    }

    @PutMapping("/{id}/availability")
    @Operation(summary = "Update doctor availability")
    public ResponseEntity<List<DoctorAvailability>> updateDoctorAvailability(
            @PathVariable Long id,
            @Valid @RequestBody List<DoctorAvailability> availabilities) {
        return ResponseEntity.ok(doctorService.updateDoctorAvailability(id, availabilities));
    }
}
