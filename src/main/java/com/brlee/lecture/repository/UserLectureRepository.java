package com.brlee.lecture.repository;

import com.brlee.lecture.entity.UserLecture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// 추가

@Repository
public interface UserLectureRepository extends JpaRepository<UserLecture, Long> {
    boolean existsByUserIdAndLectureId(Long userId, Long lectureId);
    List<UserLecture> findByUserId(Long userId);
}
