package com.project.back_end.services;

import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@org.springframework.stereotype.Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final Service service;
    private final TokenService tokenService;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    public AppointmentService(
            AppointmentRepository appointmentRepository,
            Service service,
            TokenService tokenService,
            PatientRepository patientRepository,
            DoctorRepository doctorRepository
    ) {
        this.appointmentRepository = appointmentRepository;
        this.service = service;
        this.tokenService = tokenService;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
    }

    @Transactional
    public int bookAppointment(Appointment appointment) {
        try {
            appointmentRepository.save(appointment);
            return 1;
        } catch (Exception error) {
            System.err.println(
                    "Error while booking appointment: "
                            + error.getMessage()
            );
            return 0;
        }
    }

    @Transactional
    public ResponseEntity<Map<String, String>> updateAppointment(
            Appointment appointment
    ) {
        Map<String, String> response = new HashMap<>();

        try {
            if (appointment.getId() == null) {
                response.put("message", "Appointment id is required");

                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(response);
            }

            Optional<Appointment> existingOptional =
                    appointmentRepository.findById(appointment.getId());

            if (existingOptional.isEmpty()) {
                response.put("message", "Appointment not found");

                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(response);
            }

            Appointment existingAppointment = existingOptional.get();

            if (appointment.getPatient() == null
                    || appointment.getPatient().getId() == null
                    || existingAppointment.getPatient() == null
                    || !existingAppointment.getPatient().getId()
                    .equals(appointment.getPatient().getId())) {

                response.put(
                        "message",
                        "Patient does not own this appointment"
                );

                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(response);
            }

            if (appointment.getDoctor() == null
                    || appointment.getDoctor().getId() == null
                    || !doctorRepository.existsById(
                    appointment.getDoctor().getId()
            )) {
                response.put("message", "Doctor not found");

                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(response);
            }

            boolean unchangedSlot =
                    existingAppointment.getDoctor().getId()
                            .equals(appointment.getDoctor().getId())
                            && existingAppointment.getAppointmentTime()
                            .equals(appointment.getAppointmentTime());

            if (!unchangedSlot) {
                int validation = service.validateAppointment(appointment);

                if (validation == -1) {
                    response.put("message", "Doctor not found");

                    return ResponseEntity
                            .status(HttpStatus.NOT_FOUND)
                            .body(response);
                }

                if (validation == 0) {
                    response.put(
                            "message",
                            "Appointment time is unavailable"
                    );

                    return ResponseEntity
                            .status(HttpStatus.CONFLICT)
                            .body(response);
                }
            }

            appointmentRepository.save(appointment);

            response.put("message", "Appointment updated successfully");
            return ResponseEntity.ok(response);

        } catch (Exception error) {
            System.err.println(
                    "Error while updating appointment: "
                            + error.getMessage()
            );

            response.put("message", "Internal server error");

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }
    }

    @Transactional
    public ResponseEntity<Map<String, String>> cancelAppointment(
            long id,
            String token
    ) {
        Map<String, String> response = new HashMap<>();

        try {
            Optional<Appointment> appointmentOptional =
                    appointmentRepository.findById(id);

            if (appointmentOptional.isEmpty()) {
                response.put("message", "Appointment not found");

                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(response);
            }

            String identifier =
                    tokenService.extractIdentifier(token);

            Patient patient =
                    patientRepository.findByEmail(identifier);

            Appointment appointment = appointmentOptional.get();

            if (patient == null
                    || appointment.getPatient() == null
                    || !patient.getId().equals(
                    appointment.getPatient().getId()
            )) {
                response.put(
                        "message",
                        "Patient does not own this appointment"
                );

                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(response);
            }

            appointmentRepository.delete(appointment);

            response.put(
                    "message",
                    "Appointment cancelled successfully"
            );

            return ResponseEntity.ok(response);

        } catch (Exception error) {
            System.err.println(
                    "Error while cancelling appointment: "
                            + error.getMessage()
            );

            response.put("message", "Internal server error");

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }
    }

    @Transactional
    public Map<String, Object> getAppointment(
            String patientName,
            LocalDate date,
            String token
    ) {
        Map<String, Object> response = new HashMap<>();

        try {
            String identifier =
                    tokenService.extractIdentifier(token);

            Doctor doctor =
                    doctorRepository.findByEmail(identifier);

            if (doctor == null) {
                response.put("appointments", List.of());
                response.put("message", "Doctor not found");
                return response;
            }

            LocalDateTime start =
                    date.atStartOfDay();

            LocalDateTime end =
                    date.plusDays(1)
                            .atStartOfDay()
                            .minusNanos(1);

            List<Appointment> appointments;

            if (isMissingFilter(patientName)) {
                appointments =
                        appointmentRepository
                                .findByDoctorIdAndAppointmentTimeBetween(
                                        doctor.getId(),
                                        start,
                                        end
                                );
            } else {
                appointments =
                        appointmentRepository
                                .findByDoctorIdAndPatient_NameContainingIgnoreCaseAndAppointmentTimeBetween(
                                        doctor.getId(),
                                        patientName,
                                        start,
                                        end
                                );
            }

            response.put("appointments", appointments);
            return response;

        } catch (Exception error) {
            System.err.println(
                    "Error while fetching appointments: "
                            + error.getMessage()
            );

            response.put("appointments", List.of());
            response.put("message", "Internal server error");
            return response;
        }
    }

    @Transactional
    public void changeStatus(int status, long id) {
        appointmentRepository.updateStatus(status, id);
    }

    private boolean isMissingFilter(String value) {
        return value == null
                || value.isBlank()
                || "null".equalsIgnoreCase(value);
    }
}