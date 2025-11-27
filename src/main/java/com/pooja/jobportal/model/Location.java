package com.pooja.jobportal.model;

import jakarta.persistence.*;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Location {
    
    @Column(name = "city")
    private String city;
    
    @Column(name = "state")
    private String state;
    
    @Column(name = "country")
    private String country;
    
    @Column(name = "zip_code")
    private String zipCode;
    
    @Embedded
    private Coordinates coordinates;
    
    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Coordinates {
        @Column(name = "latitude")
        private Double lat;
        
        @Column(name = "longitude")
        private Double lng;
    }
}