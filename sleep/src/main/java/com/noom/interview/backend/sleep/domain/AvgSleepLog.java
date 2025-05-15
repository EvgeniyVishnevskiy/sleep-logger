package com.noom.interview.backend.sleep.domain;

import com.noom.interview.backend.sleep.enums.SleepQuality;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class AvgSleepLog {
    private Long userId;
    private LocalTime avgSleepStart;
    private LocalTime avgSleepEnd;
    private Map<SleepQuality, Integer> sleepQualityCount;
    private LocalDate startDate;
    private LocalDate endDate;
}
