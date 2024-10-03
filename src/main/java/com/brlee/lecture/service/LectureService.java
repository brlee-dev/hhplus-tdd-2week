package com.brlee.lecture.service;

import com.brlee.lecture.entity.Lecture;
import com.brlee.lecture.entity.UserLecture;
import com.brlee.lecture.dto.LectureResponseDto;
import com.brlee.lecture.repository.LectureRepository;
import com.brlee.lecture.repository.UserLectureRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// 추가

@Service
public class LectureService {

    private final LectureRepository lectureRepository;
    private final UserLectureRepository userLectureRepository;

    public LectureService(LectureRepository lectureRepository, UserLectureRepository userLectureRepository) {
        this.lectureRepository = lectureRepository;
        this.userLectureRepository = userLectureRepository;
    }

    /**
     * 특강 신청 로직 (동시성 이슈 방지)
     * @param lectureId - 신청할 특강 ID
     * @param userId - 신청하는 사용자 ID
     * @return 신청 성공 여부
     */
    @Transactional
    public synchronized boolean applyForLecture(Long lectureId, Long userId) {
        // 특강 조회
        Optional<Lecture> lectureOpt = lectureRepository.findById(lectureId);
        if (lectureOpt.isEmpty()) {
            throw new IllegalArgumentException("강의를 찾을 수 없습니다.");
        }

        Lecture lecture = lectureOpt.get();

        // 특강 신청 인원이 30명 이상인 경우 실패
        if (lecture.getCurrentApplicants() >= lecture.getMaxCapacity()) {
            throw new IllegalStateException("이미 강의 신청 인원이 30명 입니다.");
        }

        // 사용자가 이미 해당 특강을 신청했는지 확인
        if (userLectureRepository.existsByUserIdAndLectureId(userId, lectureId)) {
            return false;
        }

        // 특강 신청 처리
        lecture.setCurrentApplicants(lecture.getCurrentApplicants() + 1);
        UserLecture userLecture = new UserLecture(lecture, userId);
        userLectureRepository.save(userLecture);
        lectureRepository.save(lecture);

        return true;
    }

    /**
     * 사용자가 신청한 특강 목록 조회
     * @param userId - 조회할 사용자 ID
     * @return 신청한 특강 목록
     */
    @Transactional(readOnly = true)
    public List<LectureResponseDto> getAppliedLectures(Long userId) {
        List<UserLecture> userLectures = userLectureRepository.findByUserId(userId);
        return userLectures.stream()
                .map(userLecture -> new LectureResponseDto(userLecture.getLecture()))
                .collect(Collectors.toList());
    }

    /**
     * 신청 가능한 특강 목록 조회
     * @return 신청 가능한 특강 목록
     */
    @Transactional(readOnly = true)
    public List<LectureResponseDto> getAvailableLectures() {
        List<Lecture> lectures = lectureRepository.findAll();
        return lectures.stream()
                .map(LectureResponseDto::new)
                .collect(Collectors.toList());
    }

    /**
     * 날짜별 신청 가능한 특강 목록 조회
     * @param date - 조회할 날짜
     * @return 신청 가능한 특강 목록
     */
    @Transactional(readOnly = true)
    public List<LectureResponseDto> getAvailableLecturesByDate(LocalDate date) {
        List<Lecture> lectures = lectureRepository.findAllByDateAndCurrentApplicantsLessThan(date, 30);
        return lectures.stream()
                .map(LectureResponseDto::new)
                .collect(Collectors.toList());
    }
}
