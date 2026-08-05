package com.hotel.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotel.model.EmailOtp;
import com.hotel.model.Role;
import com.hotel.model.User;
import com.hotel.repository.EmailOtpRepository;
import com.hotel.repository.UserRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@Service
public class UserService {

    private final UserRepository repo;
    private final BCryptPasswordEncoder encoder;
    private final EmailService emailService;
    private final EmailOtpRepository emailOtpRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public UserService(
            UserRepository repo,
            BCryptPasswordEncoder encoder,
            EmailService emailService,
            EmailOtpRepository emailOtpRepository) {

        this.repo = repo;
        this.encoder = encoder;
        this.emailService = emailService;
        this.emailOtpRepository = emailOtpRepository;
    }

    /**
     * Generates and sends a six-digit OTP to the hotel owner's email.
     */
    @Transactional
    public void sendSignupOtp(String email) {

        String normalizedEmail = normalizeEmail(email);

        if (normalizedEmail.isBlank()) {
            throw new RuntimeException("Email is required");
        }

        if (repo.existsByEmail(normalizedEmail)) {
            throw new RuntimeException("Email already exists");
        }

        String code = String.format(
                "%06d",
                secureRandom.nextInt(1_000_000)
        );

        // Remove any previous OTP created for this email.
        emailOtpRepository.deleteByEmail(normalizedEmail);

        EmailOtp otp = new EmailOtp();
        otp.setEmail(normalizedEmail);

        // Store the encrypted OTP instead of storing the original code.
        otp.setCode(encoder.encode(code));
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        otp.setVerified(false);
        otp.setFailedAttempts(0);

        emailOtpRepository.save(otp);

        // If sending fails, the transaction rolls back the unused OTP record.
        emailService.sendSignupOtp(normalizedEmail, code);
    }

    /**
     * Verifies the OTP and returns a temporary signup verification token.
     */
    @Transactional
    public String verifySignupOtp(String email, String code) {

        String normalizedEmail = normalizeEmail(email);

        if (normalizedEmail.isBlank()) {
            throw new RuntimeException("Email is required");
        }

        if (code == null || code.trim().isBlank()) {
            throw new RuntimeException("Verification code is required");
        }

        EmailOtp otp = emailOtpRepository
                .findTopByEmailOrderByIdDesc(normalizedEmail)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Send a verification code first"
                        )
                );

        if (otp.getExpiresAt() == null
                || otp.getExpiresAt()
                        .isBefore(LocalDateTime.now())) {

            throw new RuntimeException(
                    "Verification code has expired"
            );
        }

        int failedAttempts = otp.getFailedAttempts() == null
                ? 0
                : otp.getFailedAttempts();

        if (failedAttempts >= 5) {
            throw new RuntimeException(
                    "Too many incorrect attempts. Send a new code"
            );
        }

        if (!encoder.matches(code.trim(), otp.getCode())) {

            otp.setFailedAttempts(failedAttempts + 1);
            emailOtpRepository.save(otp);

            throw new RuntimeException(
                    "Incorrect verification code"
            );
        }

        String verificationToken = UUID.randomUUID().toString();

        otp.setVerified(true);
        otp.setVerificationToken(verificationToken);
        emailOtpRepository.save(otp);

        return verificationToken;
    }

    /**
     * Creates a customer or verified hotel-owner account.
     */
    @Transactional
    public User signup(User user) {

        validateSignupData(user);

        user.setEmail(normalizeEmail(user.getEmail()));

        if (repo.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        if (user.getRole() == null) {
            user.setRole(Role.CUSTOMER);
        }

        /*
         * Hotel owners must verify their email before their account
         * is inserted into the users table.
         */
        if (user.getRole() == Role.HOTEL_OWNER) {

            String verificationToken =
                    user.getEmailVerificationToken();

            if (verificationToken == null
                    || verificationToken.isBlank()) {

                throw new RuntimeException(
                        "Verify your email OTP before signing up"
                );
            }

            EmailOtp verifiedOtp = emailOtpRepository
                    .findByEmailAndVerificationToken(
                            user.getEmail(),
                            verificationToken
                    )
                    .filter(otp ->
                            Boolean.TRUE.equals(otp.getVerified())
                    )
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Verify your email OTP before signing up"
                            )
                    );

            if (verifiedOtp.getExpiresAt() == null
                    || verifiedOtp.getExpiresAt()
                            .isBefore(LocalDateTime.now())) {

                throw new RuntimeException(
                        "Email verification has expired"
                );
            }

            user.setEmailVerified(true);
            user.setActiveStatus(true);
            user.setVerificationToken(null);
            user.setVerificationTokenExpiresAt(null);

        } else {

            user.setEmailVerified(true);
            user.setActiveStatus(true);
        }

        // Password must always be encrypted before saving.
        user.setPassword(encoder.encode(user.getPassword()));

        User savedUser = repo.save(user);

        if (savedUser.getRole() == Role.HOTEL_OWNER) {

            // The OTP cannot be reused after successful registration.
            emailOtpRepository.deleteByEmail(savedUser.getEmail());

            /*
             * A welcome-email failure must not roll back the successfully
             * created database account.
             */
            try {
                emailService.sendWelcomeEmail(savedUser);
            } catch (RuntimeException mailError) {
                System.err.println(
                        "Account created, but welcome email failed for "
                                + savedUser.getEmail()
                                + ": "
                                + mailError.getMessage()
                );
            }
        }

        return savedUser;
    }

    /**
     * Finds a user after removing spaces and ignoring email case.
     */
    public Optional<User> findByEmail(String email) {

        String normalizedEmail = normalizeEmail(email);

        if (normalizedEmail.isBlank()) {
            return Optional.empty();
        }

        return repo.findByEmailIgnoreCase(normalizedEmail);
    }

    public Optional<User> findById(Integer id) {

        if (id == null) {
            return Optional.empty();
        }

        return repo.findById(id);
    }

    /**
     * Compares a raw login password with the encrypted database password.
     */
    public boolean matchPassword(
            String rawPassword,
            String encodedPassword) {

        if (rawPassword == null
                || encodedPassword == null) {
            return false;
        }

        return encoder.matches(
                rawPassword,
                encodedPassword
        );
    }

    public String encodePassword(String rawPassword) {

        if (rawPassword == null || rawPassword.isBlank()) {
            throw new RuntimeException("Password is required");
        }

        return encoder.encode(rawPassword);
    }

    public User save(User user) {

        if (user == null) {
            throw new RuntimeException("User is required");
        }

        return repo.save(user);
    }

    public List<User> getAllUsers() {
        return repo.findAll();
    }

    public void deleteUser(Integer id) {

        if (id == null) {
            throw new RuntimeException("User ID is required");
        }

        if (!repo.existsById(id)) {
            throw new RuntimeException("User not found");
        }

        repo.deleteById(id);
    }

    /**
     * Converts all email addresses into one consistent database format.
     */
    private String normalizeEmail(String email) {

        if (email == null) {
            return "";
        }

        return email.trim().toLowerCase();
    }
    
    /**
     * Returns one page of users, with newest accounts displayed first.
     */
    public Page<User> getUsers(int page, int size) {

        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);

        PageRequest pageable = PageRequest.of(
                safePage,
                safeSize,
                Sort.by(Sort.Direction.DESC, "userId")
        );

        return repo.findAll(pageable);
    }

    /**
     * Validates required signup information before accessing the database.
     */
    private void validateSignupData(User user) {

        if (user == null) {
            throw new RuntimeException(
                    "Signup information is required"
            );
        }

        if (user.getFullName() == null
                || user.getFullName().trim().isBlank()) {

            throw new RuntimeException(
                    "Full name is required"
            );
        }

        if (user.getEmail() == null
                || user.getEmail().trim().isBlank()) {

            throw new RuntimeException(
                    "Email is required"
            );
        }

        if (user.getPassword() == null
                || user.getPassword().length() < 6) {

            throw new RuntimeException(
                    "Password must contain at least 6 characters"
            );
            
            
        }
    }
    
    @Transactional
    public void sendPasswordResetOtp(String email) {
        String normalizedEmail =
                email == null ? "" : email.trim().toLowerCase();

        if (normalizedEmail.isBlank()) {
            throw new RuntimeException("Email is required");
        }

        repo.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() ->
                        new RuntimeException(
                                "No account found for this email"
                        )
                );

        String code = String.format(
                "%06d",
                secureRandom.nextInt(1_000_000)
        );

        // Remove any previously generated OTP.
        emailOtpRepository.deleteByEmail(normalizedEmail);

        EmailOtp otp = new EmailOtp();
        otp.setEmail(normalizedEmail);

        // Store the OTP securely as a BCrypt hash.
        otp.setCode(encoder.encode(code));
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        otp.setVerified(false);
        otp.setFailedAttempts(0);

        emailOtpRepository.save(otp);

        emailService.sendPasswordResetOtp(
                normalizedEmail,
                code
        );
    }
    @Transactional
    public void resetPassword(
            String email,
            String code,
            String newPassword
    ) {
        String normalizedEmail =
                email == null ? "" : email.trim().toLowerCase();

        if (normalizedEmail.isBlank()) {
            throw new RuntimeException("Email is required");
        }

        if (newPassword == null || newPassword.length() < 6) {
            throw new RuntimeException(
                    "New password must contain at least 6 characters"
            );
        }

        EmailOtp otp = emailOtpRepository
                .findTopByEmailOrderByIdDesc(normalizedEmail)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Send a password reset code first"
                        )
                );

        if (otp.getExpiresAt() == null
                || otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException(
                    "Password reset code has expired"
            );
        }

        if (otp.getFailedAttempts() != null
                && otp.getFailedAttempts() >= 5) {
            throw new RuntimeException(
                    "Too many incorrect attempts. Send a new code"
            );
        }

        String enteredCode =
                code == null ? "" : code.trim();

        if (!encoder.matches(enteredCode, otp.getCode())) {
            int attempts = otp.getFailedAttempts() == null
                    ? 0
                    : otp.getFailedAttempts();

            otp.setFailedAttempts(attempts + 1);
            emailOtpRepository.save(otp);

            throw new RuntimeException(
                    "Incorrect password reset code"
            );
        }

        User user = repo.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );

        // Never save a plain-text password.
        user.setPassword(encoder.encode(newPassword));
        repo.save(user);

        // OTP cannot be reused after a successful reset.
        emailOtpRepository.deleteByEmail(normalizedEmail);
    }
}