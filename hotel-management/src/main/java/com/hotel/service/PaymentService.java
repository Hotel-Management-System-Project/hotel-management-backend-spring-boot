package com.hotel.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotel.model.Booking;
import com.hotel.model.Payment;
import com.hotel.repository.PaymentRepository;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingService bookingService;
    private final JsonMapper jsonMapper;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Value("${razorpay.key-id:}")
    private String keyId;

    @Value("${razorpay.key-secret:}")
    private String keySecret;

    public PaymentService(PaymentRepository paymentRepository,
                          BookingService bookingService,
                          JsonMapper jsonMapper) {
        this.paymentRepository = paymentRepository;
        this.bookingService = bookingService;
        this.jsonMapper = jsonMapper;
    }

    @Transactional
    public Payment createCashPayment(Integer bookingId) {
        Booking booking = bookingService.getById(bookingId);
        if (paymentRepository.existsByBooking_BookingIdAndMethod(
                bookingId, Payment.Method.CASH)) {
            throw new IllegalStateException("Cash payment is already registered");
        }

        Payment payment = basePayment(booking, Payment.Method.CASH);
        payment.setStatus(Payment.Status.PENDING);
        return paymentRepository.save(payment);
    }

    @Transactional
    public Map<String, Object> createRazorpayOrder(Integer bookingId) {
        requireKeys();
        Booking booking = bookingService.getById(bookingId);
        Payment payment = basePayment(booking, Payment.Method.RAZORPAY);
        payment.setStatus(Payment.Status.PENDING);

        try {
            String receipt = "booking_" + bookingId + "_" + System.currentTimeMillis();
            String requestJson = jsonMapper.writeValueAsString(Map.of(
                    "amount", payment.getAmountPaise(),
                    "currency", payment.getCurrency(),
                    "receipt", receipt
            ));

            String credentials = Base64.getEncoder().encodeToString(
                    (keyId + ":" + keySecret).getBytes(StandardCharsets.UTF_8));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.razorpay.com/v1/orders"))
                    .header("Authorization", "Basic " + credentials)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new RuntimeException("Razorpay order creation failed: " + response.body());
            }

            JsonNode body = jsonMapper.readTree(response.body());
            payment.setRazorpayOrderId(body.path("id").asString());
            paymentRepository.save(payment);

            return Map.of(
                    "keyId", keyId,
                    "orderId", payment.getRazorpayOrderId(),
                    "amount", payment.getAmountPaise(),
                    "currency", payment.getCurrency(),
                    "bookingId", bookingId
            );
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Razorpay request was interrupted", exception);
        } catch (Exception exception) {
            throw new RuntimeException(exception.getMessage(), exception);
        }
    }

    @Transactional
    public Payment verify(String orderId, String paymentId, String signature) {
        requireKeys();
        requireText(orderId, "Razorpay order ID");
        requireText(paymentId, "Razorpay payment ID");
        requireText(signature, "Razorpay signature");

        Payment payment = paymentRepository.findByRazorpayOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment order not found"));
        if (payment.getStatus() == Payment.Status.PAID) return payment;
        paymentRepository.findByRazorpayPaymentId(paymentId).ifPresent(existing -> {
            if (!existing.getPaymentId().equals(payment.getPaymentId())) {
                throw new IllegalStateException("Payment ID was already processed");
            }
        });

        String expected = hmac(orderId + "|" + paymentId, keySecret);
        boolean valid = MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                signature.getBytes(StandardCharsets.UTF_8));
        if (!valid) {
            payment.setStatus(Payment.Status.FAILED);
            paymentRepository.save(payment);
            throw new RuntimeException("Payment signature verification failed");
        }

        String gatewayStatus = fetchPaymentStatus(paymentId);
        if (!"captured".equalsIgnoreCase(gatewayStatus)) {
            throw new RuntimeException(
                    "Payment is verified but not captured. Current status: " + gatewayStatus);
        }

        payment.setRazorpayPaymentId(paymentId);
        payment.setStatus(Payment.Status.PAID);
        payment.setPaidAt(LocalDateTime.now());
        return paymentRepository.save(payment);
    }

    public Payment getByRazorpayOrderId(String orderId) {
        return paymentRepository.findByRazorpayOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment order not found"));
    }

    public Payment getLatestByBooking(Integer bookingId) {
        return paymentRepository
                .findByBooking_BookingIdOrderByCreatedAtDesc(bookingId)
                .stream()
                .findFirst()
                .orElse(null);
    }

    private Payment basePayment(Booking booking, Payment.Method method) {
        long amountPaise = Math.round(booking.getTotalAmount() * 100D);
        if (amountPaise <= 0) throw new IllegalArgumentException("Invalid booking amount");
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setMethod(method);
        payment.setAmountPaise(amountPaise);
        payment.setCurrency("INR");
        return payment;
    }

    private String hmac(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new RuntimeException("Unable to verify payment", exception);
        }
    }

    private String fetchPaymentStatus(String paymentId) {
        try {
            String credentials = Base64.getEncoder().encodeToString(
                    (keyId + ":" + keySecret).getBytes(StandardCharsets.UTF_8));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.razorpay.com/v1/payments/" + paymentId))
                    .header("Authorization", "Basic " + credentials)
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new RuntimeException("Unable to confirm Razorpay payment status");
            }
            return jsonMapper.readTree(response.body()).path("status").asString();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Payment status request was interrupted", exception);
        } catch (Exception exception) {
            throw new RuntimeException(exception.getMessage(), exception);
        }
    }

    private void requireKeys() {
        requireText(keyId, "Razorpay key ID");
        requireText(keySecret, "Razorpay key secret");
    }

    private void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(field + " is not configured");
        }
    }
}
