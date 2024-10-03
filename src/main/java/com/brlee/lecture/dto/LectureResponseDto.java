package com.brlee.lecture.dto;

import com.brlee.lecture.entity.Lecture;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// 추가

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LectureResponseDto {
    private Long id;
    private String title;
    private String lecturer;
    private String date; // 문자열로 변경하거나 LocalDate로 사용 가능
    private int maxCapacity;
    private int currentApplicants;

    public LectureResponseDto(Lecture lecture) {
        this.id = lecture.getId();
        this.title = lecture.getTitle();
        this.lecturer = lecture.getLecturer();
        this.date = lecture.getDate().toString();
        this.maxCapacity = lecture.getMaxCapacity();
        this.currentApplicants = lecture.getCurrentApplicants();
    }
}
