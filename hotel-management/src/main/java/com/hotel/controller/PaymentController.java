package com.hotel.controller;

import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.hotel.dto.RazorpayVerifyRequest;
import com.hotel.model.Booking;
import com.hotel.model.Payment;
import com.hotel.model.User;
import com.hotel.service.BookingService;
import com.hotel.service.BookingRoomService;
import com.hotel.service.PaymentService;
import com.hotel.service.UserService;
import com.hotel.utils.Resp;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin("*")
public class PaymentController {

    private final PaymentService paymentService;
    private final BookingService bookingService;
    private final UserService userService;
    private final BookingRoomService bookingRoomService;

    public PaymentController(PaymentService paymentService,
                             BookingService bookingService,
                             UserService userService,
                             BookingRoomService bookingRoomService) {
        this.paymentService = paymentService;
        this.bookingService = bookingService;
        this.userService = userService;
        this.bookingRoomService = bookingRoomService;
    }

    @GetMapping("/booking/{bookingId}")
    public Resp<?> getBookingPayment(@PathVariable Integer bookingId,
                                     Authentication auth) {
        requireBookingAccess(bookingId, auth);
        Payment payment = paymentService.getLatestByBooking(bookingId);

        if (payment == null) {
            return Resp.success(Map.of(
                    "bookingId", bookingId,
                    "method", "NOT_SELECTED",
                    "status", "NOT_RECORDED"
            ));
        }

        return Resp.success(Map.of(
                "paymentId", payment.getPaymentId(),
                "bookingId", bookingId,
                "method", payment.getMethod(),
                "status", payment.getStatus(),
                "amount", payment.getAmountPaise() / 100.0,
                "currency", payment.getCurrency()
        ));
    }

    @PostMapping("/cash/{bookingId}")
    public Resp<?> cash(@PathVariable Integer bookingId, Authentication auth) {
        requireOwner(bookingId, auth);
        Payment payment = paymentService.createCashPayment(bookingId);
        return Resp.success(Map.of(
                "paymentId", payment.getPaymentId(),
                "method", payment.getMethod(),
                "status", payment.getStatus(),
                "message", "Cash payment will be collected at the hotel"
        ));
    }

    @PostMapping("/razorpay/order/{bookingId}")
    public Resp<?> createOrder(@PathVariable Integer bookingId, Authentication auth) {
        requireOwner(bookingId, auth);
        return Resp.success(paymentService.createRazorpayOrder(bookingId));
    }

    @PostMapping("/razorpay/verify")
    public Resp<?> verify(@RequestBody RazorpayVerifyRequest request,
                          Authentication auth) {
        Payment pending = paymentService.getByRazorpayOrderId(
                request.getRazorpayOrderId());
        requireOwner(pending.getBooking().getBookingId(), auth);
        Payment payment = paymentService.verify(
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature());
        return Resp.success(Map.of(
                "paymentId", payment.getPaymentId(),
                "status", payment.getStatus(),
                "message", "Online payment verified successfully"
        ));
    }

    private void requireOwner(Integer bookingId, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("Unauthorized");
        }
        User user = userService.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Booking booking = bookingService.getById(bookingId);
        boolean admin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!admin && !booking.getUser().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("Unauthorized");
        }
    }

    private void requireBookingAccess(Integer bookingId, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("Unauthorized");
        }

        User user = userService.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Booking booking = bookingService.getById(bookingId);
        boolean admin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean customer = booking.getUser().getUserId().equals(user.getUserId());
        boolean hotelOwner = bookingRoomService.getRoomsByBooking(bookingId)
                .stream()
                .anyMatch(item -> item.getRoom() != null
                        && item.getRoom().getHotel() != null
                        && item.getRoom().getHotel().getOwner() != null
                        && item.getRoom().getHotel().getOwner().getUserId()
                                .equals(user.getUserId()));

        if (!admin && !customer && !hotelOwner) {
            throw new RuntimeException("Unauthorized");
        }
    }
}
