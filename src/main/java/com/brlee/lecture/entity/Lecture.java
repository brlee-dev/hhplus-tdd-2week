package com.brlee.lecture.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// 추가

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Lecture {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String lecturer;
    private LocalDate date;

    @Column(nullable = false)
    private int maxCapacity = 30;

    @Column(nullable = false)
    private int currentApplicants = 0;
}
