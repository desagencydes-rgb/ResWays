package com.resways.app.models;

public class SurpriseBag {
    private Long id;
    private User restaurant;
    private String name;
    private double oldPrice;
    private double newPrice;
    private double distanceKm;
    private double lat;
    private double lng;
    private String status;
    private String reservationCode;
    
    // For Gson parsing of the nested restaurant object
    public static class User {
        private Long id;
        private String name;
        
        public Long getId() { return id; }
        public String getName() { return name; }
        
        public void setId(Long id) { this.id = id; }
        public void setName(String name) { this.name = name; }
    }
    
    public SurpriseBag() {}

    public Long getId() { return id; }
    
    // Derived from the nested object to match existing UI code
    public String getRestaurantName() { 
        return restaurant != null ? restaurant.getName() : "Unknown Restaurant"; 
    }
    
    public String getName() { return name; }
    public double getOldPrice() { return oldPrice; }
    public double getNewPrice() { return newPrice; }
    public double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }
    public double getLat() { return lat; }
    public double getLng() { return lng; }
    public void setLat(double lat) { this.lat = lat; }
    public void setLng(double lng) { this.lng = lng; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getReservationCode() { return reservationCode; }
    public void setReservationCode(String reservationCode) { this.reservationCode = reservationCode; }

    public void setId(Long id) { this.id = id; }
    public void setRestaurant(User restaurant) { this.restaurant = restaurant; }
    public void setName(String name) { this.name = name; }
    public void setOldPrice(double oldPrice) { this.oldPrice = oldPrice; }
    public void setNewPrice(double newPrice) { this.newPrice = newPrice; }
}
