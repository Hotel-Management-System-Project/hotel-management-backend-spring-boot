package com.hotel.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.security.core.Authentication;
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

    private final HotelService hotelService;

    public HotelController(
            HotelService hotelService
    ) {
        this.hotelService = hotelService;
    }

    /**
     * Checks whether the authenticated user has the supplied role.
     */
    private boolean hasRole(
            Authentication authentication,
            String role
    ) {
        if (authentication == null) {
            return false;
        }

        return authentication
                .getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority()
                                .equals("ROLE_" + role)
                );
    }

    /**
     * Converts the Hotel entity into a safe response DTO.
     */
    private HotelResponse toResponse(Hotel hotel) {

        HotelResponse response = new HotelResponse();

        response.setHotelId(
                hotel.getHotelId()
        );

        response.setOwnerId(
                hotel.getOwner() == null
                        ? null
                        : hotel.getOwner().getUserId()
        );

        response.setHotelName(
                hotel.getHotelName()
        );

        response.setDescription(
                hotel.getDescription()
        );

        response.setAddress(
                hotel.getAddress()
        );

        response.setCity(
                hotel.getCity()
        );

        response.setState(
                hotel.getState()
        );

        response.setPincode(
                hotel.getPincode()
        );

        response.setRating(
                hotel.getRating() == null
                        ? BigDecimal.ZERO
                        : BigDecimal.valueOf(
                                hotel.getRating()
                        )
        );

        response.setStatus(
                hotel.getStatus() == null
                        ? null
                        : hotel.getStatus().name()
        );

        response.setCreatedAt(
                hotel.getCreatedAt()
        );

        return response;
    }

    /**
     * Adds a new hotel draft.
     *
     * Hotel owners and administrators can create hotels.
     */
   
    @PostMapping
    public Resp<?> addHotel(
            @RequestBody HotelRequest request,
            Authentication authentication
    ) {
        if (authentication == null
                || !authentication.isAuthenticated()) {
            return Resp.error("Authentication is required");
        }

        boolean allowed = authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority()
                                .equals("ROLE_HOTEL_OWNER")
                        || authority.getAuthority()
                                .equals("ROLE_ADMIN")
                );

        if (!allowed) {
            return Resp.error(
                    "Only hotel owners and administrators can add hotels"
            );
        }

        System.out.println(
                "POST /api/hotels authenticated user: "
                        + authentication.getName()
                        + ", roles: "
                        + authentication.getAuthorities()
        );

        return Resp.success(
                toResponse(hotelService.addHotel(request))
        );
    }

    /**
     * Returns hotels based on the current user role.
     *
     * Admin: submitted hotel requests.
     * Owner: only their hotels.
     * Customer: only approved hotels.
     */
    @GetMapping
    public Resp<List<HotelResponse>> getAllHotels(
            Authentication authentication
    ) {
        if (authentication == null) {
            return Resp.error(
                    "Authentication is required"
            );
        }

        boolean isAdmin = hasRole(
                authentication,
                "ADMIN"
        );

        boolean isOwner = hasRole(
                authentication,
                "HOTEL_OWNER"
        );

        List<HotelResponse> hotels =
                hotelService.getVisibleHotels(
                        authentication.getName(),
                        isAdmin,
                        isOwner
                )
                .stream()
                .map(this::toResponse)
                .toList();

        return Resp.success(hotels);
    }

    /**
     * Returns one hotel using its database ID.
     */
    @GetMapping("/{id}")
    public Resp<HotelResponse> getHotelById(
            @PathVariable Integer id,
            Authentication authentication
    ) {
        if (authentication == null) {
            return Resp.error(
                    "Authentication is required"
            );
        }

        Hotel hotel =
                hotelService.getById(id);

        return Resp.success(
                toResponse(hotel)
        );
    }

    /**
     * Updates the editable details of a hotel.
     *
     * Owners can update only their own hotels. Administrators can update any
     * hotel, but ownership, rating, rooms, images, and approval status are not
     * changed by this operation.
     */
    @PutMapping("/{id}")
    public Resp<?> updateHotel(
            @PathVariable Integer id,
            @RequestBody HotelRequest request,
            Authentication authentication
    ) {
        if (authentication == null) {
            return Resp.error("Authentication is required");
        }

        boolean isOwner = hasRole(authentication, "HOTEL_OWNER");
        boolean isAdmin = hasRole(authentication, "ADMIN");

        if (!isOwner && !isAdmin) {
            return Resp.error(
                    "Only hotel owners and administrators can update hotels"
            );
        }

        Hotel updatedHotel = hotelService.updateHotel(
                id,
                request,
                authentication.getName(),
                isAdmin
        );

        return Resp.success(toResponse(updatedHotel));
    }

    /**
     * Submits a completed hotel draft for administrator approval.
     *
     * A hotel must have at least one room before submission.
     */
    @PutMapping("/{id}/submit")
    public Resp<?> submitHotelForApproval(
            @PathVariable Integer id,
            Authentication authentication
    ) {
        if (authentication == null) {
            return Resp.error(
                    "Authentication is required"
            );
        }

        if (!hasRole(
                authentication,
                "HOTEL_OWNER"
        )) {
            return Resp.error(
                    "Only a hotel owner can submit a hotel request"
            );
        }

        Hotel hotel =
                hotelService.submitForApproval(
                        id,
                        authentication.getName()
                );

        return Resp.success(
                toResponse(hotel)
        );
    }

    /**
     * Allows the administrator to approve a submitted hotel.
     */
    @PutMapping("/approve/{id}")
    public Resp<?> approveHotel(
            @PathVariable Integer id,
            Authentication authentication
    ) {
        if (authentication == null) {
            return Resp.error(
                    "Authentication is required"
            );
        }

        if (!hasRole(
                authentication,
                "ADMIN"
        )) {
            return Resp.error(
                    "Only an administrator can approve hotels"
            );
        }

        Hotel approvedHotel =
                hotelService.approve(id);

        return Resp.success(
                toResponse(approvedHotel)
        );
    }

    /**
     * Allows the administrator to reject a submitted hotel.
     */
    @PutMapping("/reject/{id}")
    public Resp<?> rejectHotel(
            @PathVariable Integer id,
            Authentication authentication
    ) {
        if (authentication == null) {
            return Resp.error(
                    "Authentication is required"
            );
        }

        if (!hasRole(
                authentication,
                "ADMIN"
        )) {
            return Resp.error(
                    "Only an administrator can reject hotels"
            );
        }

        Hotel rejectedHotel =
                hotelService.reject(id);

        return Resp.success(
                toResponse(rejectedHotel)
        );
    }

    /**
     * Deletes a hotel and its associated records.
     *
     * Owners can delete only their hotels.
     * Administrators can delete any hotel.
     */
    @DeleteMapping("/{id}")
    public Resp<?> deleteHotel(
            @PathVariable Integer id,
            Authentication authentication
    ) {
        if (authentication == null) {
            return Resp.error(
                    "Authentication is required"
            );
        }

        boolean isOwner = hasRole(
                authentication,
                "HOTEL_OWNER"
        );

        boolean isAdmin = hasRole(
                authentication,
                "ADMIN"
        );

        if (!isOwner && !isAdmin) {
            return Resp.error(
                    "Only hotel owners and administrators can delete hotels"
            );
        }

        hotelService.deleteHotel(
                id,
                authentication.getName(),
                isAdmin
        );

        return Resp.success(
                "Hotel and all related rooms deleted successfully"
        );
    }
}
