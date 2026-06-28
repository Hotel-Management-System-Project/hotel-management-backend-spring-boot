package com.hotel.model;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.*;
import lombok.*;
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "hotels")
public class Hotel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer hotelId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    private String hotelName;
    private String description;
    private String address;
    private String city;
    private String state;
    private String pincode;

    private Double rating = 0.0;

    @Enumerated(EnumType.STRING)
    private Status status;

    private LocalDateTime createdAt;

    public enum Status {
        PENDING, APPROVED, REJECTED
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("hotel-rooms")
    private List<Room> rooms;

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("hotel-images")
    private List<HotelImage> images;
}