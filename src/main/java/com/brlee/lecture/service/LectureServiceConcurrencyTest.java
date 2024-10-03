package com.brlee.lecture.service;

import com.brlee.lecture.entity.Lecture;
import com.brlee.lecture.LectureApplication;
import com.brlee.lecture.repository.LectureRepository;
import com.brlee.lecture.repository.UserLectureRepository;
import jakarta.persistence.PessimisticLockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
// 추가된 import
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.PessimisticLockingFailureException;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = LectureApplication.class)
public class LectureServiceConcurrencyTest {

	@Autowired
	private LectureService lectureService;

	@Autowired
	private LectureRepository lectureRepository;

	@Autowired
	private UserLectureRepository userLectureRepository;

	private Long lectureId;

	@BeforeEach
	void setUp() {
		// 테스트 전에 특강을 초기화하고 DB에 저장
		Lecture lecture = new Lecture();
		lecture.setTitle("Concurrent Test Lecture");
		lecture.setLecturer("John Doe");
		lecture.setMaxCapacity(30);  // 최대 30명만 가능
		lecture.setCurrentApplicants(0);  // 현재 신청 인원 0
		lectureRepository.save(lecture);
		lectureId = lecture.getId();  // 강의 ID 저장
	}

	@Test
	void testLectureApplicationConcurrency() throws InterruptedException {
		int numberOfThreads = 40;  // 40명이 동시에 신청
		ExecutorService executorService = Executors.newFixedThreadPool(40);  // 40개의 스레드 풀
		CountDownLatch latch = new CountDownLatch(numberOfThreads);  // 40개의 작업이 완료될 때까지 기다림

		for (int i = 0; i < numberOfThreads; i++) {
			final Long userId = (long) (i + 1);  // 각 사용자에게 다른 ID 부여

			executorService.submit(() -> {
				try {
					lectureService.applyForLecture(lectureId, userId);  // 신청 시도
					System.out.println("User " + userId + " applied successfully");
				} catch (CannotAcquireLockException e) {
					System.out.println("User " + userId + " failed to acquire lock (CannotAcquireLockException)");
				} catch (PessimisticLockingFailureException e) {
					System.out.println("User " + userId + " failed to acquire lock (PessimisticLockingFailureException)");
				} catch (Exception e) {
					System.out.println("User " + userId + " failed: " + e.getMessage());
				} finally {
					latch.countDown();  // 작업 완료 후 카운트 감소
				}
			});
		}

		latch.await(30, TimeUnit.SECONDS);  // 모든 스레드가 완료될 때까지 최대 30초 대기
		executorService.shutdown();  // 스레드 풀 종료

		// 최종적으로 30명만 신청에 성공했는지 검증
		Lecture lecture = lectureRepository.findById(lectureId).orElseThrow();
		assertEquals(30, lecture.getCurrentApplicants());  // 최대 신청 인원이 30명인지 확인

		// 실제로 30명의 유저가 성공했는지 검증
		long successfulApplications = userLectureRepository.count();
		assertEquals(30, successfulApplications);
	}
}
