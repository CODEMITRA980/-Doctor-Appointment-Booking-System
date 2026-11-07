package com.luminacare.patientservice.service;

import com.luminacare.common.exception.BusinessException;
import com.luminacare.common.exception.ResourceNotFoundException;
import com.luminacare.patientservice.entity.Patient;
import com.luminacare.patientservice.event.PatientEventProducer;
import com.luminacare.patientservice.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private PatientEventProducer patientEventProducer;

    @InjectMocks
    private PatientService patientService;

    private Patient testPatient;

    @BeforeEach
    void setUp() {
        testPatient = Patient.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("1234567890")
                .dob(LocalDate.of(1990, 1, 1))
                .gender("Male")
                .address("123 Main St")
                .bloodGroup("O+")
                .emergencyContact("0987654321")
                .build();
    }

    @Test
    void testGetAllPatients() {
        when(patientRepository.findAll()).thenReturn(List.of(testPatient));

        List<Patient> result = patientService.getAllPatients();

        assertEquals(1, result.size());
        assertEquals("John", result.get(0).getFirstName());
        verify(patientRepository, times(1)).findAll();
    }

    @Test
    void testGetPatientById_Found() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));

        Optional<Patient> result = patientService.getPatientById(1L);

        assertTrue(result.isPresent());
        assertEquals("John", result.get().getFirstName());
    }

    @Test
    void testGetPatientById_NotFound() {
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Patient> result = patientService.getPatientById(99L);

        assertFalse(result.isPresent());
    }

    @Test
    void testCreatePatient_Success() {
        when(patientRepository.findByEmail(testPatient.getEmail())).thenReturn(Optional.empty());
        when(patientRepository.save(any(Patient.class))).thenReturn(testPatient);

        Patient created = patientService.createPatient(testPatient);

        assertNotNull(created);
        assertEquals("john.doe@example.com", created.getEmail());
        verify(patientEventProducer, times(1)).publishPatientCreatedEvent(created);
    }

    @Test
    void testCreatePatient_DuplicateEmail_ThrowsBusinessException() {
        when(patientRepository.findByEmail(testPatient.getEmail())).thenReturn(Optional.of(testPatient));

        assertThrows(BusinessException.class, () -> patientService.createPatient(testPatient));
        verify(patientRepository, never()).save(any());
        verify(patientEventProducer, never()).publishPatientCreatedEvent(any());
    }

    @Test
    void testUpdatePatient_Success() {
        Patient updatedData = Patient.builder()
                .firstName("Jane")
                .lastName("Smith")
                .phone("9999999999")
                .build();

        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
        when(patientRepository.save(any(Patient.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Patient updated = patientService.updatePatient(1L, updatedData);

        assertEquals("Jane", updated.getFirstName());
        assertEquals("Smith", updated.getLastName());
        assertEquals("9999999999", updated.getPhone());
        assertEquals("john.doe@example.com", updated.getEmail()); // preserved
        verify(patientEventProducer, times(1)).publishPatientUpdatedEvent(updated);
    }

    @Test
    void testUpdatePatient_NotFound_ThrowsResourceNotFoundException() {
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> patientService.updatePatient(99L, testPatient));
    }

    @Test
    void testUpdatePatient_DuplicateEmail_ThrowsBusinessException() {
        Patient existingOther = Patient.builder()
                .id(2L)
                .email("other@example.com")
                .build();

        Patient updateData = Patient.builder()
                .email("other@example.com")
                .build();

        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
        when(patientRepository.findByEmail("other@example.com")).thenReturn(Optional.of(existingOther));

        assertThrows(BusinessException.class, () -> patientService.updatePatient(1L, updateData));
    }

    @Test
    void testDeletePatient_Success() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));

        patientService.deletePatient(1L);

        verify(patientRepository, times(1)).delete(testPatient);
    }

    @Test
    void testDeletePatient_NotFound_ThrowsResourceNotFoundException() {
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> patientService.deletePatient(99L));
        verify(patientRepository, never()).delete(any());
    }

    @Test
    void testSearchPatients() {
        when(patientRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase("doe", "doe"))
                .thenReturn(List.of(testPatient));

        List<Patient> result = patientService.searchPatients("doe");

        assertEquals(1, result.size());
        assertEquals("Doe", result.get(0).getLastName());
    }
}
