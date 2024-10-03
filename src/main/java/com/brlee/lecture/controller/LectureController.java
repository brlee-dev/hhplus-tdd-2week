package com.brlee.lecture.controller;

import com.brlee.lecture.dto.LectureRequestDto;
import com.brlee.lecture.dto.LectureResponseDto;
import com.brlee.lecture.service.LectureService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

// 추가

@RestController
@RequestMapping("/api/lectures")
public class LectureController {

    private final LectureService lectureService;

    public LectureController(LectureService lectureService) {
        this.lectureService = lectureService;
    }

    /**
     * 특강 신청 API
     * @param requestDto - 신청 정보 (특강 ID, 사용자 ID)
     * @return 신청 성공 여부에 따른 메시지
     */
    @PostMapping("/apply")
    public ResponseEntity<String> applyForLecture(@RequestBody LectureRequestDto requestDto) {
        try {
            boolean isApplied = lectureService.applyForLecture(requestDto.getLectureId(), requestDto.getUserId());

            if (isApplied) {
                return ResponseEntity.ok("강의 신청이 완료 되었습니다.");
            } else {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 신청한 강의 입니다.");
            }
        } catch (IllegalStateException e) {
            // 정원이 찬 경우 처리
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            // 특강이 존재하지 않는 경우 처리
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /**
     * 신청한 특강 목록 조회 API
     * @param userId - 조회할 사용자 ID
     * @return 해당 사용자가 신청한 특강 목록
     */
    @GetMapping("/applied/{userId}")
    public ResponseEntity<List<LectureResponseDto>> getAppliedLectures(@PathVariable Long userId) {
        List<LectureResponseDto> lectures = lectureService.getAppliedLectures(userId);
        return ResponseEntity.ok(lectures);
    }

    /**
     * 현재 신청 가능한 특강 목록 조회 API
     * @return 신청 가능한 특강 목록
     */
    @GetMapping("/list")
    public ResponseEntity<List<LectureResponseDto>> getAvailableLectures() {
        List<LectureResponseDto> lectures = lectureService.getAvailableLectures();
        return ResponseEntity.ok(lectures);
    }

    /**
     * 날짜별 신청 가능한 특강 목록 조회 API
     * @param date - 조회할 날짜 (형식: YYYY-MM-DD)
     * @return 신청 가능한 특강 목록
     */
    @GetMapping("/available")
    public ResponseEntity<List<LectureResponseDto>> getAvailableLecturesByDate(@RequestParam("date") LocalDate date) {
        List<LectureResponseDto> lectures = lectureService.getAvailableLecturesByDate(date);
        return ResponseEntity.ok(lectures);
    }
}
