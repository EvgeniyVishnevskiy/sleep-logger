package com.noom.interview.backend.sleep.service;


import com.noom.interview.backend.sleep.db.service.DailySleepLogService;
import com.noom.interview.backend.sleep.dto.DailySleepLog;
import com.noom.interview.backend.sleep.exception.SleepLogAlreadyExistsException;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

    void checkOverlaps(DailySleepLog dailySleepLog) {
        Long userId = dailySleepLog.getUserId();
        LocalDateTime newLogStart = dailySleepLog.getSleepStart();
        LocalDateTime newLogEnd = dailySleepLog.getSleepEnd();
        LocalDateTime queryRangeStart = newLogStart.toLocalDate().atStartOfDay();
        LocalDateTime queryRangeEnd = newLogEnd.toLocalDate().atTime(LocalTime.MAX);

        List<DailySleepLog> potentialConflicts = service.findByUserIdAndInterval(
          userId,
          queryRangeStart,
          queryRangeEnd
        );

        for (DailySleepLog existingLog : potentialConflicts) {
            if (dailySleepLog.getId() != null && dailySleepLog.getId().equals(existingLog.getId())) {
                continue;
            }

            LocalDateTime existingLogStart = existingLog.getSleepStart();
            LocalDateTime existingLogEnd = existingLog.getSleepEnd();

            boolean newLogEndsStrictlyBeforeExistingStarts = newLogEnd.isBefore(existingLogStart);
            boolean newLogStartsStrictlyAfterExistingEnds = newLogStart.isAfter(existingLogEnd);

            if (!(newLogEndsStrictlyBeforeExistingStarts || newLogStartsStrictlyAfterExistingEnds)) {
                throw new SleepLogAlreadyExistsException(
                  "You already have a log between " +
                    existingLogStart.toString() + " and " + existingLogEnd.toString() +
                    ". The conflicting period is " +
                    newLogStart.toString() + " to " + newLogEnd.toString() + "."
                );
            }
        }
    }

}
