package com.noom.interview.backend.sleep.service;


import com.noom.interview.backend.sleep.db.service.DailySleepLogService;
import com.noom.interview.backend.sleep.dto.AvgSleepLog;
import com.noom.interview.backend.sleep.dto.DailySleepLog;
import com.noom.interview.backend.sleep.enums.SleepQuality;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GetLastXDays {

    private final DailySleepLogService service;

    @Autowired
    public GetLastXDays(DailySleepLogService service) {
        this.service = service;
    }

    public AvgSleepLog execute(Long userId, Long numberOfDays) {
        List<DailySleepLog> result = service.findByUserIdAndInterval(userId, LocalDate.now().atStartOfDay().minusDays(numberOfDays), LocalDateTime.now());

        if (Objects.isNull(result) || result.isEmpty()) {
            return null;
        }

        AtomicInteger totalSleepStart = new AtomicInteger();
        AtomicInteger totalSleepEnd = new AtomicInteger();
        Map<SleepQuality, Integer> sleepQualityCount = new HashMap<>();
        sleepQualityCount.put(SleepQuality.BAD, 0);
        sleepQualityCount.put(SleepQuality.OK, 0);
        sleepQualityCount.put(SleepQuality.GOOD, 0);

        result.forEach(log -> {
            totalSleepStart.addAndGet(log.getSleepStart().toLocalTime().toSecondOfDay());
            totalSleepEnd.addAndGet(log.getSleepEnd().toLocalTime().toSecondOfDay());
            sleepQualityCount.put(log.getSleepQuality(), sleepQualityCount.get(log.getSleepQuality()) + 1);
        });

        LocalTime avgSleepStart = LocalTime.ofSecondOfDay(totalSleepStart.get() / result.size());
        LocalTime avgSleepEnd = LocalTime.ofSecondOfDay(totalSleepEnd.get() / result.size());

        return AvgSleepLog.builder()
          .userId(userId)
          .avgSleepStart(avgSleepStart)
          .avgSleepEnd(avgSleepEnd)
          .sleepQualityCount(sleepQualityCount)
          .startDate(LocalDate.now().minusMonths(1))
          .endDate(LocalDate.now())
          .build();
    }
}
