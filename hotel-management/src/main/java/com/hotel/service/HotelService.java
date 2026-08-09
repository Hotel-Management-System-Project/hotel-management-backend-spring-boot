package com.hotel.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotel.dto.HotelRequest;
import com.hotel.model.Hotel;
import com.hotel.model.User;
import com.hotel.repository.HotelRepository;
import com.hotel.repository.UserRepository;

@Service
public class HotelService {

    private final HotelRepository hotelRepository;
    private final UserRepository userRepository;

    public HotelService(
            HotelRepository hotelRepository,
            UserRepository userRepository
    ) {
        this.hotelRepository = hotelRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Hotel addHotel(HotelRequest request) {

        if (request == null) {
            throw new RuntimeException(
                    "Hotel information is required"
            );
        }

        if (request.getOwnerId() == null) {
            throw new RuntimeException(
                    "Hotel owner ID is required"
            );
        }

        User owner = userRepository
                .findById(request.getOwnerId())
                .orElseThrow(() ->
                    new RuntimeException(
                        "Hotel owner not found"
                    )
                );

        Hotel hotel = new Hotel();

        hotel.setOwner(owner);
        hotel.setHotelName(request.getHotelName());
        hotel.setDescription(request.getDescription());
        hotel.setAddress(request.getAddress());
        hotel.setCity(request.getCity());
        hotel.setState(request.getState());
        hotel.setPincode(request.getPincode());

        /*
         * Do not accept rating during hotel registration.
         * Customer reviews should determine the rating later.
         */
        hotel.setRating(0.0);

        /*
         * The hotel remains private while compulsory rooms
         * and images are being added.
         */
        hotel.setStatus(Hotel.Status.DRAFT);

        return hotelRepository.save(hotel);
    }

    public Hotel getById(Integer hotelId) {
        return hotelRepository
                .findById(hotelId)
                .orElseThrow(() ->
                    new RuntimeException("Hotel not found")
                );
    }

    @Transactional
    public Hotel updateHotel(
            Integer hotelId,
            HotelRequest request,
            String requesterEmail,
            boolean isAdmin
    ) {
        if (request == null) {
            throw new RuntimeException("Hotel information is required");
        }

        if (isBlank(request.getHotelName())
                || isBlank(request.getAddress())
                || isBlank(request.getCity())
                || isBlank(request.getState())
                || isBlank(request.getPincode())) {
            throw new RuntimeException(
                    "Hotel name, address, city, state, and pincode are required"
            );
        }

        Hotel hotel = getById(hotelId);
        boolean isOwner = hotel.getOwner() != null
                && hotel.getOwner().getEmail()
                        .equalsIgnoreCase(requesterEmail);

        if (!isAdmin && !isOwner) {
            throw new RuntimeException("You can update only your own hotel");
        }

        hotel.setHotelName(request.getHotelName().trim());
        hotel.setDescription(
                request.getDescription() == null
                        ? null
                        : request.getDescription().trim()
        );
        hotel.setAddress(request.getAddress().trim());
        hotel.setCity(request.getCity().trim());
        hotel.setState(request.getState().trim());
        hotel.setPincode(request.getPincode().trim());

        return hotelRepository.save(hotel);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public List<Hotel> getAll() {
        return hotelRepository.findAll();
    }

    public List<Hotel> getVisibleHotels(
            String email,
            boolean isAdmin,
            boolean isOwner
    ) {
        if (isOwner) {
            User owner = userRepository
                    .findByEmail(email)
                    .orElseThrow(() ->
                        new RuntimeException("Owner not found")
                    );

            return hotelRepository
                    .findByOwner_UserId(
                        owner.getUserId()
                    );
        }

        if (isAdmin) {
            /*
             * Administrators see submitted requests,
             * but not incomplete drafts.
             */
            return hotelRepository
                    .findAll()
                    .stream()
                    .filter(hotel ->
                        hotel.getStatus()
                                != Hotel.Status.DRAFT
                    )
                    .toList();
        }

        /*
         * Customers see approved hotels only.
         */
        return hotelRepository
                .findAll()
                .stream()
                .filter(hotel ->
                    hotel.getStatus()
                            == Hotel.Status.APPROVED
                )
                .toList();
    }

    @Transactional
    public Hotel submitForApproval(
            Integer hotelId,
            String requesterEmail
    ) {
        Hotel hotel = getById(hotelId);

        if (hotel.getOwner() == null
                || !hotel.getOwner()
                    .getEmail()
                    .equalsIgnoreCase(requesterEmail)) {

            throw new RuntimeException(
                    "You can submit only your own hotel"
            );
        }

        if (hotel.getStatus() != Hotel.Status.DRAFT
                && hotel.getStatus()
                        != Hotel.Status.REJECTED) {

            throw new RuntimeException(
                    "This hotel has already been submitted"
            );
        }

        if (hotel.getRooms() == null
                || hotel.getRooms().isEmpty()) {

            throw new RuntimeException(
                    "Add at least one room before submitting the hotel"
            );
        }

        hotel.setStatus(Hotel.Status.PENDING);

        return hotelRepository.save(hotel);
    }

    @Transactional
    public Hotel approve(Integer hotelId) {
        Hotel hotel = getById(hotelId);

        if (hotel.getStatus() != Hotel.Status.PENDING) {
            throw new RuntimeException(
                    "Only a pending hotel can be approved"
            );
        }

        hotel.setStatus(Hotel.Status.APPROVED);

        return hotelRepository.save(hotel);
    }

    @Transactional
    public Hotel reject(Integer hotelId) {
        Hotel hotel = getById(hotelId);

        if (hotel.getStatus() != Hotel.Status.PENDING) {
            throw new RuntimeException(
                    "Only a pending hotel can be rejected"
            );
        }

        hotel.setStatus(Hotel.Status.REJECTED);

        return hotelRepository.save(hotel);
    }

    @Transactional
    public void deleteHotel(
            Integer hotelId,
            String requesterEmail,
            boolean isAdmin
    ) {
        Hotel hotel = getById(hotelId);

        boolean isOwner =
                hotel.getOwner() != null
                && hotel.getOwner()
                    .getEmail()
                    .equalsIgnoreCase(requesterEmail);

        if (!isAdmin && !isOwner) {
            throw new RuntimeException(
                    "You can delete only your own hotel"
            );
        }

        hotelRepository.delete(hotel);
    }
}
