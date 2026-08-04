package com.project.back_end.services;

import com.project.back_end.models.Prescription;
import com.project.back_end.repo.PrescriptionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;

    public PrescriptionService(
            PrescriptionRepository prescriptionRepository
    ) {
        this.prescriptionRepository = prescriptionRepository;
    }

    public ResponseEntity<Map<String, String>> savePrescription(
            Prescription prescription
    ) {
        Map<String, String> response = new HashMap<>();

        try {
            List<Prescription> existingPrescriptions =
                    prescriptionRepository.findByAppointmentId(
                            prescription.getAppointmentId()
                    );

            if (!existingPrescriptions.isEmpty()) {
                response.put(
                        "message",
                        "Prescription already exists for this appointment"
                );

                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(response);
            }

            prescriptionRepository.save(prescription);

            response.put("message", "Prescription saved");

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(response);

        } catch (Exception error) {
            System.err.println(
                    "Error while saving prescription: "
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

    public ResponseEntity<Map<String, Object>> getPrescription(
            Long appointmentId
    ) {
        Map<String, Object> response = new HashMap<>();

        try {
            List<Prescription> prescriptions =
                    prescriptionRepository.findByAppointmentId(
                            appointmentId
                    );

            if (prescriptions.isEmpty()) {
                response.put(
                        "message",
                        "No prescription found for this appointment"
                );

                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(response);
            }

            response.put("prescription", prescriptions);

            return ResponseEntity.ok(response);

        } catch (Exception error) {
            System.err.println(
                    "Error while fetching prescription: "
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
}