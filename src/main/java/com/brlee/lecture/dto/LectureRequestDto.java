package com.brlee.lecture.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// 추가

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LectureRequestDto {
    private Long lectureId;
    private Long userId;
}
