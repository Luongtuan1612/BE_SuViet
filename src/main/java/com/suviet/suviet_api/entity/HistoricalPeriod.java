package com.suviet.suviet_api.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "historical_periods")
@Data // Của thư viện Lombok, giúp tự động tạo Getter, Setter, Constructor
public class HistoricalPeriod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "start_year")
    private Integer startYear;

    @Column(name = "end_year")
    private Integer endYear;

    @Column(columnDefinition = "TEXT")
    private String summary;
    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 255)
    private String image;
}