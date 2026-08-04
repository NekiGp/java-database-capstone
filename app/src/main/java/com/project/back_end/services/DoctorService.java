package com.project.back_end.services;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;

    public DoctorService(
            DoctorRepository doctorRepository,
            AppointmentRepository appointmentRepository,
            TokenService tokenService
    ) {
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
    }

    @Transactional
    public List<String> getDoctorAvailability(Long doctorId, LocalDate date) {
        Doctor doctor = doctorRepository.findById(doctorId).orElse(null);

        if (doctor == null || doctor.getAvailableTimes() == null) {
            return new ArrayList<>();
        }

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay().minusNanos(1);

        List<Appointment> appointments =
                appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(
                        doctorId,
                        startOfDay,
                        endOfDay
                );

        List<String> availableSlots =
                new ArrayList<>(doctor.getAvailableTimes());

        for (Appointment appointment : appointments) {
            LocalTime bookedTime =
                    appointment.getAppointmentTime().toLocalTime();

            availableSlots.removeIf(slot ->
                    getSlotStartTime(slot).equals(bookedTime)
            );
        }

        return availableSlots;
    }

    public int saveDoctor(Doctor doctor) {
        try {
            Doctor existingDoctor =
                    doctorRepository.findByEmail(doctor.getEmail());

            if (existingDoctor != null) {
                return -1;
            }

            doctorRepository.save(doctor);
            return 1;

        } catch (Exception error) {
            return 0;
        }
    }

    public int updateDoctor(Doctor doctor) {
        try {
            if (doctor.getId() == null
                    || !doctorRepository.existsById(doctor.getId())) {
                return -1;
            }

            doctorRepository.save(doctor);
            return 1;

        } catch (Exception error) {
            return 0;
        }
    }

    @Transactional
    public List<Doctor> getDoctors() {
        List<Doctor> doctors = doctorRepository.findAll();

        doctors.forEach(doctor -> {
            if (doctor.getAvailableTimes() != null) {
                doctor.getAvailableTimes().size();
            }
        });

        return doctors;
    }

    @Transactional
    public int deleteDoctor(long id) {
        try {
            if (!doctorRepository.existsById(id)) {
                return -1;
            }

            appointmentRepository.deleteAllByDoctorId(id);
            doctorRepository.deleteById(id);

            return 1;

        } catch (Exception error) {
            return 0;
        }
    }

    public ResponseEntity<Map<String, String>> validateDoctor(Login login) {
        Map<String, String> response = new HashMap<>();

        try {
            Doctor doctor =
                    doctorRepository.findByEmail(login.getIdentifier());

            if (doctor == null) {
                response.put("message", "Doctor not found");

                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(response);
            }

            if (!doctor.getPassword().equals(login.getPassword())) {
                response.put("message", "Invalid credentials");

                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(response);
            }

            String token =
                    tokenService.generateToken(doctor.getEmail());

            response.put("token", token);

            return ResponseEntity.ok(response);

        } catch (Exception error) {
            response.put("message", "Internal server error");

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }
    }

    @Transactional
    public Map<String, Object> findDoctorByName(String name) {
        Map<String, Object> response = new HashMap<>();

        List<Doctor> doctors =
                doctorRepository.findByNameLike(name);

        doctors.forEach(doctor -> {
            if (doctor.getAvailableTimes() != null) {
                doctor.getAvailableTimes().size();
            }
        });

        response.put("doctors", doctors);
        return response;
    }

    @Transactional
    public Map<String, Object> filterDoctorsByNameSpecilityandTime(
            String name,
            String specialty,
            String amOrPm
    ) {
        List<Doctor> doctors =
                doctorRepository
                        .findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(
                                name,
                                specialty
                        );

        return createDoctorResponse(
                filterDoctorByTime(doctors, amOrPm)
        );
    }

    @Transactional
    public Map<String, Object> filterDoctorByNameAndTime(
            String name,
            String amOrPm
    ) {
        List<Doctor> doctors =
                doctorRepository.findByNameLike(name);

        return createDoctorResponse(
                filterDoctorByTime(doctors, amOrPm)
        );
    }

    @Transactional
    public Map<String, Object> filterDoctorByNameAndSpecility(
            String name,
            String specialty
    ) {
        List<Doctor> doctors =
                doctorRepository
                        .findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(
                                name,
                                specialty
                        );

        return createDoctorResponse(doctors);
    }

    @Transactional
    public Map<String, Object> filterDoctorByTimeAndSpecility(
            String specialty,
            String amOrPm
    ) {
        List<Doctor> doctors =
                doctorRepository.findBySpecialtyIgnoreCase(specialty);

        return createDoctorResponse(
                filterDoctorByTime(doctors, amOrPm)
        );
    }

    @Transactional
    public Map<String, Object> filterDoctorBySpecility(
            String specialty
    ) {
        List<Doctor> doctors =
                doctorRepository.findBySpecialtyIgnoreCase(specialty);

        return createDoctorResponse(doctors);
    }

    @Transactional
    public Map<String, Object> filterDoctorsByTime(String amOrPm) {
        List<Doctor> doctors = doctorRepository.findAll();

        return createDoctorResponse(
                filterDoctorByTime(doctors, amOrPm)
        );
    }

    private List<Doctor> filterDoctorByTime(
            List<Doctor> doctors,
            String timeFilter
    ) {
        if (timeFilter == null || timeFilter.isBlank()) {
            return doctors;
        }

        List<Doctor> filteredDoctors = new ArrayList<>();

        for (Doctor doctor : doctors) {
            List<String> availableTimes =
                    doctor.getAvailableTimes();

            if (availableTimes == null) {
                continue;
            }

            boolean matches = availableTimes.stream()
                    .anyMatch(slot ->
                            matchesTimeFilter(slot, timeFilter)
                    );

            if (matches) {
                filteredDoctors.add(doctor);
            }
        }

        return filteredDoctors;
    }

    private boolean matchesTimeFilter(
            String slot,
            String timeFilter
    ) {
        if (slot == null || timeFilter == null) {
            return false;
        }

        if (slot.equalsIgnoreCase(timeFilter)) {
            return true;
        }

        LocalTime startTime = getSlotStartTime(slot);

        if ("AM".equalsIgnoreCase(timeFilter)) {
            return startTime.isBefore(LocalTime.NOON);
        }

        if ("PM".equalsIgnoreCase(timeFilter)) {
            return !startTime.isBefore(LocalTime.NOON);
        }

        return false;
    }

    private LocalTime getSlotStartTime(String slot) {
        String start = slot.split("-")[0].trim();
        return LocalTime.parse(start);
    }

    private Map<String, Object> createDoctorResponse(
            List<Doctor> doctors
    ) {
        Map<String, Object> response = new HashMap<>();
        response.put("doctors", doctors);
        return response;
    }
}