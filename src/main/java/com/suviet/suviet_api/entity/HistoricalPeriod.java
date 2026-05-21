package com.suviet.suviet_api.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "historical_periods")
@Data // Của thư viện Lombok, giúp tự động tạo Getter, Setter, Constructor
public class HistoricalPeriod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Tự động tăng ID (Auto Increment)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name; // Tên triều đại (VD: Thời Bắc thuộc, Nhà Lý)

    @Column(name = "start_year")
    private Integer startYear; // Năm bắt đầu

    @Column(name = "end_year")
    private Integer endYear; // Năm kết thúc

    @Column(columnDefinition = "TEXT")
    private String summary; // Tóm tắt ngắn gọn
    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 255)
    private String image;
}