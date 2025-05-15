package com.noom.interview.backend.sleep.db.service;


import com.noom.interview.backend.sleep.dto.DailySleepLog;
import java.time.LocalDateTime;
import java.util.List;

public interface DailySleepLogService {

    DailySleepLog save(DailySleepLog dailySleepLog);

    List<DailySleepLog> findByUserIdAndInterval(Long userId, LocalDateTime start, LocalDateTime end);
}
