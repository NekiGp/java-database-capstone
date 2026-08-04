package com.project.back_end.services;

import com.project.back_end.repo.AdminRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class TokenService {

    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    @Value("${jwt.secret}")
    private String jwtSecret;

    public TokenService(
            AdminRepository adminRepository,
            DoctorRepository doctorRepository,
            PatientRepository patientRepository
    ) {
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(
                jwtSecret.getBytes(StandardCharsets.UTF_8)
        );
    }

    public String generateToken(String identifier) {
        Date issuedAt = new Date();

        long sevenDays =
                7L * 24L * 60L * 60L * 1000L;

        Date expiration =
                new Date(issuedAt.getTime() + sevenDays);

        return Jwts.builder()
                .subject(identifier)
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(getSigningKey())
                .compact();
    }

    public String extractIdentifier(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean validateToken(String token, String user) {
        try {
            if (token == null || token.isBlank()) {
                return false;
            }

            String identifier = extractIdentifier(token);

            if (identifier == null || identifier.isBlank()) {
                return false;
            }

            return switch (user.toLowerCase()) {
                case "admin" ->
                        adminRepository.findByUsername(identifier) != null;

                case "doctor" ->
                        doctorRepository.findByEmail(identifier) != null;

                case "patient" ->
                        patientRepository.findByEmail(identifier) != null;

                default -> false;
            };

        } catch (Exception error) {
            return false;
        }
    }
}