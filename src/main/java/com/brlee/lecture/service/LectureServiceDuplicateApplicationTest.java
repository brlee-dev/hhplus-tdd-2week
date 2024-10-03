package com.brlee.lecture.service;

import com.brlee.lecture.entity.Lecture;
import com.brlee.lecture.LectureApplication;
import com.brlee.lecture.repository.LectureRepository;
import com.brlee.lecture.repository.UserLectureRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = LectureApplication.class)
public class LectureServiceDuplicateApplicationTest {

    @Autowired
    private LectureService lectureService;

    @Autowired
    private LectureRepository lectureRepository;

    @Autowired
    private UserLectureRepository userLectureRepository;

    private Long lectureId;
    private final Long userId = 1L; // 동일한 사용자 ID

    @BeforeEach
    void setUp() {
        // 테스트 전에 특강을 초기화하고 DB에 저장
        Lecture lecture = new Lecture();
        lecture.setTitle("Duplicate Application Test Lecture");
        lecture.setLecturer("John Doe");
        lecture.setMaxCapacity(30);  // 최대 30명만 가능
        lecture.setCurrentApplicants(0);  // 현재 신청 인원 0
        lectureRepository.save(lecture);
        lectureId = lecture.getId();  // 강의 ID 저장
    }

    @Test
    @Transactional
    void testDuplicateLectureApplication() throws InterruptedException {
        int numberOfAttempts = 5;  // 동일 사용자가 5번 신청 시도
        ExecutorService executorService = Executors.newFixedThreadPool(5);  // 5개의 스레드 풀
        CountDownLatch latch = new CountDownLatch(numberOfAttempts);  // 5개의 작업이 완료될 때까지 기다림

        for (int i = 0; i < numberOfAttempts; i++) {
            executorService.submit(() -> {
                try {
                    boolean result = lectureService.applyForLecture(lectureId, userId);  // 신청 시도
                    System.out.println("Application result: " + result);  // 로그 추가
                } finally {
                    latch.countDown();  // 작업 완료 후 카운트 감소
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);  // 모든 스레드가 완료될 때까지 최대 10초 대기
        executorService.shutdown();  // 스레드 풀 종료

        // 최종적으로 1번만 신청에 성공했는지 검증
        long successfulApplications = userLectureRepository.countByUserIdAndLectureId(userId, lectureId);
        assertEquals(1, successfulApplications);  // 1번만 성공했는지 확인

        // 신청 인원도 1명만 증가했는지 검증
        Lecture lecture = lectureRepository.findById(lectureId).orElseThrow();
        assertEquals(1, lecture.getCurrentApplicants());  // 신청 인원이 1명인지 확인
    }
}
