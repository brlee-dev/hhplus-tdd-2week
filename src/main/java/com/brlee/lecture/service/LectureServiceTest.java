package com.brlee.lecture.service;

import com.brlee.lecture.dto.LectureResponseDto;
import com.brlee.lecture.entity.Lecture;
import com.brlee.lecture.entity.UserLecture;
import com.brlee.lecture.repository.LectureRepository;
import com.brlee.lecture.repository.UserLectureRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;



import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LectureServiceTest {

	@InjectMocks
	private LectureService lectureService;

	@Mock
	private LectureRepository lectureRepository;

	@Mock
	private UserLectureRepository userLectureRepository;

	private Lecture lecture;
	private final Long lectureId = 1L;
	private final Long userId = 1L;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this); // Mock 객체 초기화
		lecture = new Lecture();
		lecture.setId(lectureId);
		lecture.setTitle("Spring Boot 특강");
		lecture.setLecturer("John Doe");
		lecture.setMaxCapacity(30);
		lecture.setCurrentApplicants(10);  // 현재 신청 인원 설정
		lecture.setDate(LocalDate.of(2024, 10, 10)); // 날짜 필드 설정
	}

	/**
	 * 성공 케이스 테스트
	 */
	@Test
	void applyForLecture_Success() {
		// given
		when(lectureRepository.findById(lectureId)).thenReturn(Optional.of(lecture));
		when(userLectureRepository.existsByUserIdAndLectureId(userId, lectureId)).thenReturn(false);

		// when
		boolean result = lectureService.applyForLecture(lectureId, userId);

		// then
		assertTrue(result);  // 신청 성공
		verify(lectureRepository, times(1)).save(lecture);
		verify(userLectureRepository, times(1)).save(any(UserLecture.class));
		assertEquals(11, lecture.getCurrentApplicants());  // 신청 인원 증가 확인
	}

	/**
	 * 동일 신청자가 두 번 신청하면 막아야 되는 케이스 테스트
	 */
	@Test
	void applyForLecture_DuplicateApplication_Fail() {
		// given
		when(lectureRepository.findById(lectureId)).thenReturn(Optional.of(lecture));
		when(userLectureRepository.existsByUserIdAndLectureId(userId, lectureId)).thenReturn(true);  // 이미 신청한 경우

		// when
		boolean result = lectureService.applyForLecture(lectureId, userId);

		// then
		assertFalse(result);  // 신청 실패
		verify(lectureRepository, never()).save(lecture);  // 중복 신청이므로 저장하지 않음
		verify(userLectureRepository, never()).save(any(UserLecture.class));
	}

	/**
	 * 정원 초과 시 신청 실패 케이스 테스트
	 */
	@Test
	void applyForLecture_OverCapacity_Fail() {
		// given
		lecture.setCurrentApplicants(30);  // 정원이 다 찬 경우
		when(lectureRepository.findById(lectureId)).thenReturn(Optional.of(lecture));
		when(userLectureRepository.existsByUserIdAndLectureId(userId, lectureId)).thenReturn(false);

		// when
		IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
			lectureService.applyForLecture(lectureId, userId);
		});

		// then
		assertEquals("이미 강의 신청 인원이 30명 입니다.", exception.getMessage());  // 정원 초과 메시지 확인
		verify(lectureRepository, never()).save(lecture);  // 정원이 다 찼으므로 저장하지 않음
		verify(userLectureRepository, never()).save(any(UserLecture.class));
	}

	/**
	 * 존재하지 않는 강의 신청 시 예외 처리 테스트
	 */
	@Test
	 void applyForLecture_LectureNotFound_Exception() {
		// given
		when(lectureRepository.findById(lectureId)).thenReturn(Optional.empty());  // 강의가 없는 경우

		// when
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
			lectureService.applyForLecture(lectureId, userId);
		});

		// then
		assertEquals("강의를 찾을 수 없습니다.", exception.getMessage());  // 강의가 없다는 메시지 확인
		verify(lectureRepository, never()).save(lecture);  // 강의가 없으므로 저장하지 않음
		verify(userLectureRepository, never()).save(any(UserLecture.class));
	}

	@Test
	void getAvailableLecturesByDate_NoFullCapacityLectures() {
		// given
		LocalDate date = LocalDate.of(2024, 10, 10);

		// 신청 가능한 특강 (정원 미달)
		Lecture availableLecture = new Lecture();
		availableLecture.setId(1L);
		availableLecture.setTitle("Java Spring 특강");
		availableLecture.setLecturer("Jane Doe");
		availableLecture.setDate(date);
		availableLecture.setMaxCapacity(30);
		availableLecture.setCurrentApplicants(20); // 정원 미달

		// 정원이 꽉 찬 특강
		Lecture fullLecture = new Lecture();
		fullLecture.setId(2L);
		fullLecture.setTitle("Docker 특강");
		fullLecture.setLecturer("John Smith");
		fullLecture.setDate(date);
		fullLecture.setMaxCapacity(30);
		fullLecture.setCurrentApplicants(30); // 정원이 다 찬 상태

		// 리스트에 추가
		List<Lecture> lectures = new ArrayList<>();
		lectures.add(availableLecture);
		lectures.add(fullLecture);

		when(lectureRepository.findAllByDateAndCurrentApplicantsLessThan(date, 30))
				.thenReturn(List.of(availableLecture)); // 정원이 꽉찬 특강 제외한 목록 반환

		// when
		List<LectureResponseDto> result = lectureService.getAvailableLecturesByDate(date);

		// then
		assertEquals(1, result.size());  // 정원이 꽉찬 특강 제외한 1개만 반환
		assertEquals(availableLecture.getTitle(), result.get(0).getTitle());
		assertEquals(availableLecture.getLecturer(), result.get(0).getLecturer());
		verify(lectureRepository, times(1)).findAllByDateAndCurrentApplicantsLessThan(date, 30);
	}

	/**
	 * 신청 가능한 특강이 없는 경우 테스트
	 */
	@Test
	void getAvailableLecturesByDate_NoAvailableLectures() {
		// given
		LocalDate date = LocalDate.of(2024, 10, 10);

		// 해당 날짜에 신청 가능한 특강이 없는 경우
		when(lectureRepository.findAllByDateAndCurrentApplicantsLessThan(date, 30))
				.thenReturn(new ArrayList<>());  // 빈 리스트 반환

		// when
		List<LectureResponseDto> result = lectureService.getAvailableLecturesByDate(date);

		// then
		assertTrue(result.isEmpty());  // 신청 가능한 특강이 없으므로 결과가 빈 리스트여야 함
		verify(lectureRepository, times(1)).findAllByDateAndCurrentApplicantsLessThan(date, 30);
	}
	/**
	 * 수강 신청 완료된 특강 목록 조회 - 신청된 특강이 있을 경우
	 */
	@Test
	void getAppliedLectures_UserHasAppliedLectures() {
		// given
		UserLecture userLecture = new UserLecture(lecture, userId);  // 사용자와 연결된 특강
		List<UserLecture> appliedLectures = List.of(userLecture);  // 사용자 신청 목록

		when(userLectureRepository.findByUserId(userId)).thenReturn(appliedLectures);

		// when
		List<LectureResponseDto> result = lectureService.getAppliedLectures(userId);

		// then
		assertEquals(1, result.size());  // 사용자가 신청한 특강이 1개
		assertEquals(lecture.getTitle(), result.get(0).getTitle());  // 제목이 같은지 확인
		assertEquals(lecture.getLecturer(), result.get(0).getLecturer());  // 강사가 같은지 확인
		verify(userLectureRepository, times(1)).findByUserId(userId);  // Repository 호출 확인
	}

	/**
	 * 수강 신청 완료된 특강 목록 조회 - 신청된 특강이 없을 경우
	 */
	@Test
	void getAppliedLectures_UserHasNoAppliedLectures() {
		// given
		when(userLectureRepository.findByUserId(userId)).thenReturn(new ArrayList<>());  // 신청된 특강이 없음

		// when
		List<LectureResponseDto> result = lectureService.getAppliedLectures(userId);

		// then
		assertTrue(result.isEmpty());  // 신청된 특강이 없으므로 빈 리스트여야 함
		verify(userLectureRepository, times(1)).findByUserId(userId);  // Repository 호출 확인
	}
}
