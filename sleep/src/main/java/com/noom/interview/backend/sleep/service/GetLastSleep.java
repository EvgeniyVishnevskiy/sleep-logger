package com.noom.interview.backend.sleep.service;

import com.noom.interview.backend.sleep.db.service.DailySleepLogService;
import com.noom.interview.backend.sleep.dto.DailySleepLog;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GetLastSleep {

    private final DailySleepLogService service;

    @Autowired
    public GetLastSleep(DailySleepLogService service) {
        this.service = service;
    }

    public DailySleepLog execute(Long userId) {
        return service
            .findByUserIdAndInterval(userId, LocalDate.now().atStartOfDay(), LocalDateTime.now())
            .stream()
            .findFirst()
            .orElse(null);
    }
}
