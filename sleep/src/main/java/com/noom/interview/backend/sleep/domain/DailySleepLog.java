package com.noom.interview.backend.sleep.domain;

import com.noom.interview.backend.sleep.enums.SleepQuality;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DailySleepLog {
    private UUID id;
    private Long userId;
    private LocalDateTime sleepStart;
    private LocalDateTime sleepEnd;
    private SleepQuality sleepQuality;
}
