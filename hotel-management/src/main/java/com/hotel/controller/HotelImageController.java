package com.hotel.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hotel.dto.HotelImageDTO;
import com.hotel.dto.HotelImageRequestDTO;
import com.hotel.model.Hotel;
import com.hotel.model.HotelImage;
import com.hotel.service.HotelImageService;
import com.hotel.utils.Resp;

@RestController
@RequestMapping("/api/hotel-images")
public class HotelImageController {

    private final HotelImageService service;

    public HotelImageController(HotelImageService service) {
        this.service = service;
    }

    private boolean isValidImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return false;
        }

        try {
            java.net.URI uri = java.net.URI.create(imageUrl.trim());
            String scheme = uri.getScheme();
            return uri.getHost() != null
                    && ("http".equalsIgnoreCase(scheme)
                    || "https".equalsIgnoreCase(scheme));
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    @PostMapping
    public Resp<?> add(@RequestBody HotelImageRequestDTO dto) {
        if (dto.getHotelId() == null) {
            return Resp.error("Hotel id is required");
        }

        if (!isValidImageUrl(dto.getImageUrl())) {
            return Resp.error("A valid HTTP or HTTPS image URL is required");
        }

        HotelImage img = new HotelImage();

        Hotel hotel = new Hotel();
        hotel.setHotelId(dto.getHotelId());

        img.setHotel(hotel);
        img.setImageUrl(dto.getImageUrl().trim());

        return Resp.success(service.add(img));
    }

    
    @GetMapping("/hotel/{hotelId}")
    public List<HotelImageDTO> getImages(@PathVariable Integer hotelId) {
        return service.getByHotel(hotelId)
                .stream()
                .map(img -> {
                    HotelImageDTO dto = new HotelImageDTO();
                    dto.setImageId(img.getImageId());
                    dto.setImageUrl(img.getImageUrl());
                    return dto;
                }).toList();
    }
    
    @PutMapping("/{imageId}")
    public Resp<?> update(@PathVariable Integer imageId,
                          @RequestBody HotelImageRequestDTO dto) {

        HotelImage img = service.getById(imageId);

        if (img == null) {
            return Resp.error("Image not found");
        }

        if (!isValidImageUrl(dto.getImageUrl())) {
            return Resp.error("A valid HTTP or HTTPS image URL is required");
        }

        // update fields
        img.setImageUrl(dto.getImageUrl().trim());

        if (dto.getHotelId() != null) {
            Hotel hotel = new Hotel();
            hotel.setHotelId(dto.getHotelId());
            img.setHotel(hotel);
        }

        return Resp.success(service.add(img)); // save updated
    }
    
    
    @DeleteMapping("/{imageId}")
    public Resp<?> delete(@PathVariable Integer imageId) {

        HotelImage img = service.getById(imageId);

        if (img == null) {
            return Resp.error("Image not found");
        }

        service.delete(imageId);
        return Resp.success("Image deleted successfully");
    }
    
}
