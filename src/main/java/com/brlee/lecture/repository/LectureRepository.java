package com.brlee.lecture.repository;

import com.brlee.lecture.entity.Lecture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LectureRepository extends JpaRepository<Lecture, Long> {

    /**
     * 날짜별로 현재 신청 가능한 특강 목록 조회 (신청 인원이 정원보다 적은 특강만)
     * @param date - 조회할 날짜
     * @param maxCapacity - 최대 수강 가능 인원
     * @return 신청 가능한 특강 목록
     */
    List<Lecture> findAllByDateAndCurrentApplicantsLessThan(LocalDate date, int maxCapacity);

    // 비관적 잠금을 위한 메서드 수정
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Lecture> findById(Long id);
}
