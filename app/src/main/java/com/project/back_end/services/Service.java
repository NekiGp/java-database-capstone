package com.project.back_end.services;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Admin;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AdminRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@org.springframework.stereotype.Service
public class Service {

    private final TokenService tokenService;
    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final DoctorService doctorService;
    private final PatientService patientService;

    public Service(
            TokenService tokenService,
            AdminRepository adminRepository,
            DoctorRepository doctorRepository,
            PatientRepository patientRepository,
            DoctorService doctorService,
            PatientService patientService
    ) {
        this.tokenService = tokenService;
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.doctorService = doctorService;
        this.patientService = patientService;
    }

    public ResponseEntity<Map<String, String>> validateToken(
            String token,
            String user
    ) {
        Map<String, String> response = new HashMap<>();

        boolean validToken =
                tokenService.validateToken(token, user);

        if (validToken) {
            return ResponseEntity.ok(response);
        }

        response.put("message", "Invalid or expired token");

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(response);
    }

    public ResponseEntity<Map<String, String>> validateAdmin(
            Admin receivedAdmin
    ) {
        Map<String, String> response = new HashMap<>();

        try {
            Admin admin =
                    adminRepository.findByUsername(
                            receivedAdmin.getUsername()
                    );

            if (admin == null) {
                response.put("message", "Admin not found");

                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(response);
            }

            if (!admin.getPassword().equals(
                    receivedAdmin.getPassword()
            )) {
                response.put("message", "Invalid credentials");

                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(response);
            }

            String token =
                    tokenService.generateToken(admin.getUsername());

            response.put("token", token);
            return ResponseEntity.ok(response);

        } catch (Exception error) {
            response.put("message", "Internal server error");

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }
    }

    public Map<String, Object> filterDoctor(
            String name,
            String specialty,
            String time
    ) {
        boolean hasName = !isMissingFilter(name);
        boolean hasSpecialty = !isMissingFilter(specialty);
        boolean hasTime = !isMissingFilter(time);

        if (hasName && hasSpecialty && hasTime) {
            return doctorService
                    .filterDoctorsByNameSpecilityandTime(
                            name,
                            specialty,
                            time
                    );
        }

        if (hasName && hasTime) {
            return doctorService
                    .filterDoctorByNameAndTime(name, time);
        }

        if (hasName && hasSpecialty) {
            return doctorService
                    .filterDoctorByNameAndSpecility(
                            name,
                            specialty
                    );
        }

        if (hasSpecialty && hasTime) {
            return doctorService
                    .filterDoctorByTimeAndSpecility(
                            specialty,
                            time
                    );
        }

        if (hasName) {
            return doctorService.findDoctorByName(name);
        }

        if (hasSpecialty) {
            return doctorService
                    .filterDoctorBySpecility(specialty);
        }

        if (hasTime) {
            return doctorService.filterDoctorsByTime(time);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("doctors", doctorService.getDoctors());
        return response;
    }

    public int validateAppointment(Appointment appointment) {
        try {
            if (appointment == null
                    || appointment.getDoctor() == null
                    || appointment.getDoctor().getId() == null
                    || appointment.getAppointmentTime() == null) {
                return 0;
            }

            Long doctorId =
                    appointment.getDoctor().getId();

            Doctor doctor =
                    doctorRepository.findById(doctorId)
                            .orElse(null);

            if (doctor == null) {
                return -1;
            }

            LocalTime requestedTime =
                    appointment.getAppointmentTime()
                            .toLocalTime();

            return doctorService
                    .getDoctorAvailability(
                            doctorId,
                            appointment.getAppointmentDate()
                    )
                    .stream()
                    .map(this::getSlotStartTime)
                    .anyMatch(requestedTime::equals)
                    ? 1
                    : 0;

        } catch (Exception error) {
            return 0;
        }
    }

    public boolean validatePatient(Patient patient) {
        try {
            Patient existingPatient =
                    patientRepository.findByEmailOrPhone(
                            patient.getEmail(),
                            patient.getPhone()
                    );

            return existingPatient == null;

        } catch (Exception error) {
            return false;
        }
    }

    public ResponseEntity<Map<String, String>> validatePatientLogin(
            Login login
    ) {
        Map<String, String> response = new HashMap<>();

        try {
            Patient patient =
                    patientRepository.findByEmail(
                            login.getIdentifier()
                    );

            if (patient == null) {
                response.put("message", "Patient not found");

                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(response);
            }

            if (!patient.getPassword().equals(login.getPassword())) {
                response.put("message", "Invalid credentials");

                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(response);
            }

            String token =
                    tokenService.generateToken(patient.getEmail());

            response.put("token", token);
            return ResponseEntity.ok(response);

        } catch (Exception error) {
            response.put("message", "Internal server error");

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }
    }

    public ResponseEntity<Map<String, Object>> filterPatient(
            String condition,
            String name,
            String token
    ) {
        try {
            String identifier =
                    tokenService.extractIdentifier(token);

            Patient patient =
                    patientRepository.findByEmail(identifier);

            if (patient == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Patient not found");

                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(response);
            }

            boolean hasCondition =
                    !isMissingFilter(condition);

            boolean hasName =
                    !isMissingFilter(name);

            if (hasCondition && hasName) {
                return patientService
                        .filterByDoctorAndCondition(
                                condition,
                                name,
                                patient.getId()
                        );
            }

            if (hasCondition) {
                return patientService
                        .filterByCondition(
                                condition,
                                patient.getId()
                        );
            }

            if (hasName) {
                return patientService
                        .filterByDoctor(
                                name,
                                patient.getId()
                        );
            }

            return patientService.getPatientAppointment(
                    patient.getId(),
                    token
            );

        } catch (Exception error) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Invalid or expired token");

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(response);
        }
    }

    private boolean isMissingFilter(String value) {
        return value == null
                || value.isBlank()
                || "null".equalsIgnoreCase(value);
    }

    private LocalTime getSlotStartTime(String slot) {
        String start =
                slot.split("-")[0].trim();

        return LocalTime.parse(start);
    }
}