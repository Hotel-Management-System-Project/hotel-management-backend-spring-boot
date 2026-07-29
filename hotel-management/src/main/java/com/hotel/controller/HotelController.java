package com.hotel.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hotel.dto.HotelRequest;
import com.hotel.dto.HotelResponse;
import com.hotel.model.Hotel;
import com.hotel.service.HotelService;
import com.hotel.utils.Resp;

@RestController
@RequestMapping("/api/hotels")
public class HotelController {

    private final HotelService service;

    public HotelController(HotelService service) {
        this.service = service;
    }

    private boolean isAdmin() {
        return SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority()
                                .equals("ROLE_ADMIN")
                );
    }

    /*
     * Convert Hotel entity into DTO.
     * This prevents recursive JSON:
     * Hotel -> Rooms -> Images -> Room -> Hotel...
     */
    private HotelResponse toResponse(Hotel hotel) {
        HotelResponse response = new HotelResponse();

        response.setHotelId(hotel.getHotelId());

        response.setOwnerId(
                hotel.getOwner() == null
                        ? null
                        : hotel.getOwner().getUserId()
        );

        response.setHotelName(hotel.getHotelName());
        response.setDescription(hotel.getDescription());
        response.setAddress(hotel.getAddress());
        response.setCity(hotel.getCity());
        response.setState(hotel.getState());
        response.setPincode(hotel.getPincode());

        response.setRating(
                hotel.getRating() == null
                        ? BigDecimal.ZERO
                        : BigDecimal.valueOf(hotel.getRating())
        );

        response.setStatus(
                hotel.getStatus() == null
                        ? null
                        : hotel.getStatus().name()
        );

        response.setCreatedAt(hotel.getCreatedAt());

        return response;
    }

    // Add hotel
    @PostMapping
    public Resp<HotelResponse> add(
            @RequestBody HotelRequest request
    ) {
        Hotel savedHotel = service.addHotel(request);

        return Resp.success(
                toResponse(savedHotel)
        );
    }

    // Get every hotel
    @GetMapping
    public Resp<List<HotelResponse>> getAll() {
        List<HotelResponse> hotels = service
                .getAll()
                .stream()
                .map(this::toResponse)
                .toList();

        return Resp.success(hotels);
    }

    // Get one hotel
    @GetMapping("/{id}")
    public Resp<HotelResponse> getById(
            @PathVariable Integer id
    ) {
        Hotel hotel = service.getById(id);

        return Resp.success(
                toResponse(hotel)
        );
    }

    // Approve hotel: admin only
    @PutMapping("/approve/{id}")
    public Resp<?> approve(
            @PathVariable Integer id
    ) {
        if (!isAdmin()) {
            return Resp.error("Only Admin");
        }

        Hotel approvedHotel = service.approve(id);

        return Resp.success(
                toResponse(approvedHotel)
        );
    }

    // Reject hotel: admin only
    @PutMapping("/reject/{id}")
    public Resp<?> reject(
            @PathVariable Integer id
    ) {
        if (!isAdmin()) {
            return Resp.error("Only Admin");
        }

        Hotel rejectedHotel = service.reject(id);

        return Resp.success(
                toResponse(rejectedHotel)
        );
    }

    // Delete hotel: owner or admin
    @DeleteMapping("/{id}")
    public Resp<?> deleteHotel(
            @PathVariable Integer id,
            Authentication authentication
    ) {
        if (authentication == null) {
            return Resp.error("Unauthorized");
        }

        boolean owner = authentication
                .getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority()
                                .equals("ROLE_HOTEL_OWNER")
                );

        boolean admin = authentication
                .getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority()
                                .equals("ROLE_ADMIN")
                );

        if (!owner && !admin) {
            return Resp.error(
                    "Only hotel owners and administrators can delete hotels"
            );
        }

        service.deleteHotel(
                id,
                authentication.getName(),
                admin
        );

        return Resp.success(
                "Hotel and all rooms deleted successfully"
        );
    }
}