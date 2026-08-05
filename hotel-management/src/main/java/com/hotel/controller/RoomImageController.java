package com.hotel.controller;

import java.net.URI;
import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hotel.dto.RoomImageDTO;
import com.hotel.model.Room;
import com.hotel.model.RoomImage;
import com.hotel.service.RoomImageService;
import com.hotel.service.RoomService;
import com.hotel.utils.Resp;

@RestController
@RequestMapping("/api/room-images")
public class RoomImageController {

    private final RoomImageService service;
    private final RoomService roomService;

    public RoomImageController(RoomImageService service, RoomService roomService) {
        this.service = service;
        this.roomService = roomService;
    }

    private boolean isAdmin() {
        return SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }

    private boolean canManage(Room room) {
        if (isAdmin()) {
            return true;
        }
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return room.getHotel() != null
                && room.getHotel().getOwner() != null
                && room.getHotel().getOwner().getEmail().equalsIgnoreCase(email);
    }

    private boolean isValidUrl(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        try {
            URI uri = URI.create(value.trim());
            return uri.getHost() != null
                    && ("http".equalsIgnoreCase(uri.getScheme())
                    || "https".equalsIgnoreCase(uri.getScheme()));
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private RoomImageDTO toDTO(RoomImage image) {
        RoomImageDTO dto = new RoomImageDTO();
        dto.setImageId(image.getImageId());
        dto.setRoomId(image.getRoom().getRoomId());
        dto.setImageUrl(image.getImageUrl());
        return dto;
    }

    @GetMapping("/room/{roomId}")
    public Resp<?> getByRoom(@PathVariable Integer roomId) {
        List<RoomImageDTO> images = service.getByRoom(roomId)
                .stream().map(this::toDTO).toList();
        return Resp.success(images);
    }

    @PostMapping
    public Resp<?> add(@RequestBody RoomImageDTO dto) {
        if (dto.getRoomId() == null || !isValidUrl(dto.getImageUrl())) {
            return Resp.error("Room id and a valid HTTP/HTTPS image URL are required");
        }
        Room room = roomService.getRoomEntityById(dto.getRoomId());
        if (!canManage(room)) {
            return Resp.error("You can add images only to your own rooms");
        }
        RoomImage image = new RoomImage();
        image.setRoom(room);
        image.setImageUrl(dto.getImageUrl().trim());
        return Resp.success(toDTO(service.save(image)));
    }

    @PutMapping("/{imageId}")
    public Resp<?> update(@PathVariable Integer imageId,
                          @RequestBody RoomImageDTO dto) {
        RoomImage image = service.getById(imageId);
        if (!canManage(image.getRoom())) {
            return Resp.error("You can update images only for your own rooms");
        }
        if (!isValidUrl(dto.getImageUrl())) {
            return Resp.error("A valid HTTP/HTTPS image URL is required");
        }
        image.setImageUrl(dto.getImageUrl().trim());
        return Resp.success(toDTO(service.save(image)));
    }

    @DeleteMapping("/{imageId}")
    public Resp<?> delete(@PathVariable Integer imageId) {
        RoomImage image = service.getById(imageId);
        if (!canManage(image.getRoom())) {
            return Resp.error("You can delete images only from your own rooms");
        }
        service.delete(imageId);
        return Resp.success("Room image deleted successfully");
    }
}
