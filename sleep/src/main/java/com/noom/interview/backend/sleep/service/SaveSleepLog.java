package com.noom.interview.backend.sleep.service;


import com.noom.interview.backend.sleep.db.service.DailySleepLogService;
import com.noom.interview.backend.sleep.domain.DailySleepLog;
import com.noom.interview.backend.sleep.exception.SleepLogAlreadyExistsException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoField;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SaveSleepLog {

    private final DailySleepLogService service;


    @Autowired
    public SaveSleepLog(DailySleepLogService service) {
        this.service = service;
    }

    public DailySleepLog save(DailySleepLog dailySleepLog) {
        checkOverlaps(dailySleepLog);
        return service.save(dailySleepLog);
    }

    private void checkOverlaps(DailySleepLog dailySleepLog) {
        List<DailySleepLog> conflictLog = service.findByUserIdAndInterval(dailySleepLog.getUserId(),
            dailySleepLog.getSleepStart().with(ChronoField.NANO_OF_DAY, LocalTime.MIN.toNanoOfDay()),
            dailySleepLog.getSleepEnd().with(ChronoField.NANO_OF_DAY, LocalTime.MAX.toNanoOfDay()));

        LocalDateTime start1 = dailySleepLog.getSleepStart();
        LocalDateTime end1 = dailySleepLog.getSleepEnd();

        for(DailySleepLog log : conflictLog) {
            LocalDateTime start2 = log.getSleepStart();
            LocalDateTime end2 = log.getSleepEnd();
            if (!(end1.isBefore(start2) || start1.isAfter(end2)) || (start1.isAfter(start2) && end1.isBefore(end2))) {
                throw new SleepLogAlreadyExistsException("You already have a log between " + log.getSleepStart() + " and " + log.getSleepEnd());
            }
        }
    }
}
