package com.noom.interview.backend.sleep.db.repository;


import com.noom.interview.backend.sleep.db.entity.DailySleepLogEntity;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DailySleepLogRepository extends JpaRepository<DailySleepLogEntity, Long> {

    List<DailySleepLogEntity> findByUserIdAndSleepEndBetweenOrderBySleepEndDesc(Long userId, LocalDateTime start, LocalDateTime end);
}
