package com.brlee.lecture.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// 추가

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class UserLecture {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "lecture_id")
    private Lecture lecture;

    private Long userId;

    // 특정 필드를 받는 생성자 추가
    public UserLecture(Lecture lecture, Long userId) {
        this.lecture = lecture;
        this.userId = userId;
    }
}
