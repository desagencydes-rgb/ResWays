package com.resways.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "bags")
public class SurpriseBag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private double oldPrice;
    private double newPrice;
    private String pickupTime;
    
    // The restaurant that owns this bag
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "restaurant_id")
    private User restaurant;
    
    // Latitude and Longitude for distance calculations
    private double lat;
    private double lng;
    
    // Serialized for the client
    private double distanceKm;

    // Available, Reserved, Completed
    private String status;

    private Long reservedById; // If a customer reserves it
    private String reservationCode; // The 4 digit PIN

    public SurpriseBag() {}

    // Getters and Setters omitted for brevity but standard
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getOldPrice() { return oldPrice; }
    public void setOldPrice(double oldPrice) { this.oldPrice = oldPrice; }
    public double getNewPrice() { return newPrice; }
    public void setNewPrice(double newPrice) { this.newPrice = newPrice; }
    public String getPickupTime() { return pickupTime; }
    public void setPickupTime(String pickupTime) { this.pickupTime = pickupTime; }
    public User getRestaurant() { return restaurant; }
    public void setRestaurant(User restaurant) { this.restaurant = restaurant; }
    public double getLat() { return lat; }
    public void setLat(double lat) { this.lat = lat; }
    public double getLng() { return lng; }
    public void setLng(double lng) { this.lng = lng; }
    public double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getReservedById() { return reservedById; }
    public void setReservedById(Long reservedById) { this.reservedById = reservedById; }
    public String getReservationCode() { return reservationCode; }
    public void setReservationCode(String reservationCode) { this.reservationCode = reservationCode; }
}
