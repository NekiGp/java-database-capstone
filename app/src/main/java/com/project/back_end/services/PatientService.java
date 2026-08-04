package com.project.back_end.services;

import com.project.back_end.DTO.AppointmentDTO;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.PatientRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;

    public PatientService(
            PatientRepository patientRepository,
            AppointmentRepository appointmentRepository,
            TokenService tokenService
    ) {
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
    }

    public int createPatient(Patient patient) {
        try {
            patientRepository.save(patient);
            return 1;
        } catch (Exception error) {
            System.err.println(
                    "Error while creating patient: " + error.getMessage()
            );
            return 0;
        }
    }

    @Transactional
    public ResponseEntity<Map<String, Object>> getPatientAppointment(
            Long id,
            String token
    ) {
        Map<String, Object> response = new HashMap<>();

        try {
            String identifier = tokenService.extractIdentifier(token);
            Patient patient = patientRepository.findByEmail(identifier);

            if (patient == null || !patient.getId().equals(id)) {
                response.put(
                        "message",
                        "Unauthorized access to patient appointments"
                );

                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(response);
            }

            List<Appointment> appointments =
                    appointmentRepository.findByPatientId(id);

            List<AppointmentDTO> appointmentDTOs =
                    convertToDTOList(appointments);

            response.put("appointments", appointmentDTOs);

            return ResponseEntity.ok(response);

        } catch (Exception error) {
            System.err.println(
                    "Error while fetching appointments: "
                            + error.getMessage()
            );

            response.put(
                    "message",
                    "Internal server error"
            );

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }
    }

    @Transactional
    public ResponseEntity<Map<String, Object>> filterByCondition(
            String condition,
            Long id
    ) {
        Map<String, Object> response = new HashMap<>();

        try {
            int status;

            if ("past".equalsIgnoreCase(condition)) {
                status = 1;
            } else if (
                    "future".equalsIgnoreCase(condition)
                            || "upcoming".equalsIgnoreCase(condition)
            ) {
                status = 0;
            } else {
                response.put(
                        "message",
                        "Invalid condition. Use past, future, or upcoming"
                );

                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(response);
            }

            List<Appointment> appointments =
                    appointmentRepository
                            .findByPatient_IdAndStatusOrderByAppointmentTimeAsc(
                                    id,
                                    status
                            );

            response.put(
                    "appointments",
                    convertToDTOList(appointments)
            );

            return ResponseEntity.ok(response);

        } catch (Exception error) {
            System.err.println(
                    "Error while filtering appointments by condition: "
                            + error.getMessage()
            );

            response.put(
                    "message",
                    "Internal server error"
            );

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }
    }

    @Transactional
    public ResponseEntity<Map<String, Object>> filterByDoctor(
            String name,
            Long patientId
    ) {
        Map<String, Object> response = new HashMap<>();

        try {
            List<Appointment> appointments =
                    appointmentRepository
                            .filterByDoctorNameAndPatientId(
                                    name,
                                    patientId
                            );

            response.put(
                    "appointments",
                    convertToDTOList(appointments)
            );

            return ResponseEntity.ok(response);

        } catch (Exception error) {
            System.err.println(
                    "Error while filtering appointments by doctor: "
                            + error.getMessage()
            );

            response.put(
                    "message",
                    "Internal server error"
            );

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }
    }

    @Transactional
    public ResponseEntity<Map<String, Object>> filterByDoctorAndCondition(
            String condition,
            String name,
            long patientId
    ) {
        Map<String, Object> response = new HashMap<>();

        try {
            int status;

            if ("past".equalsIgnoreCase(condition)) {
                status = 1;
            } else if (
                    "future".equalsIgnoreCase(condition)
                            || "upcoming".equalsIgnoreCase(condition)
            ) {
                status = 0;
            } else {
                response.put(
                        "message",
                        "Invalid condition. Use past, future, or upcoming"
                );

                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(response);
            }

            List<Appointment> appointments =
                    appointmentRepository
                            .filterByDoctorNameAndPatientIdAndStatus(
                                    name,
                                    patientId,
                                    status
                            );

            response.put(
                    "appointments",
                    convertToDTOList(appointments)
            );

            return ResponseEntity.ok(response);

        } catch (Exception error) {
            System.err.println(
                    "Error while filtering appointments: "
                            + error.getMessage()
            );

            response.put(
                    "message",
                    "Internal server error"
            );

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }
    }

    public ResponseEntity<Map<String, Object>> getPatientDetails(
            String token
    ) {
        Map<String, Object> response = new HashMap<>();

        try {
            String identifier = tokenService.extractIdentifier(token);
            Patient patient = patientRepository.findByEmail(identifier);

            if (patient == null) {
                response.put(
                        "message",
                        "Patient not found"
                );

                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(response);
            }

            response.put("patient", patient);

            return ResponseEntity.ok(response);

        } catch (Exception error) {
            System.err.println(
                    "Error while fetching patient details: "
                            + error.getMessage()
            );

            response.put(
                    "message",
                    "Invalid or expired token"
            );

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(response);
        }
    }

    private List<AppointmentDTO> convertToDTOList(
            List<Appointment> appointments
    ) {
        return appointments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private AppointmentDTO convertToDTO(
            Appointment appointment
    ) {
        return new AppointmentDTO(
                appointment.getId(),
                appointment.getDoctor().getId(),
                appointment.getDoctor().getName(),
                appointment.getPatient().getId(),
                appointment.getPatient().getName(),
                appointment.getPatient().getEmail(),
                appointment.getPatient().getPhone(),
                appointment.getPatient().getAddress(),
                appointment.getAppointmentTime(),
                appointment.getStatus()
        );
    }
}