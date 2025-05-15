package com.noom.interview.backend.sleep.db.service;


import com.noom.interview.backend.sleep.db.entity.DailySleepLogEntity;
import com.noom.interview.backend.sleep.db.repository.DailySleepLogRepository;
import com.noom.interview.backend.sleep.dto.DailySleepLog;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class DailySleepLogServiceImpl implements DailySleepLogService {

    private final DailySleepLogRepository repository;

    @Override
    public DailySleepLog save(DailySleepLog dailySleepLog) {
        DailySleepLogEntity data = repository.save(toEntity(dailySleepLog));

        dailySleepLog.setId(data.getId());
        return dailySleepLog;
    }

    @Override
    public List<DailySleepLog> findByUserIdAndInterval(Long userId, LocalDateTime start, LocalDateTime end) {
        List<DailySleepLogEntity> result = repository.findByUserIdAndSleepEndBetweenOrderBySleepEndDesc(userId,
            start , end);

        return result.stream().map(this::toDomain).collect(Collectors.toList());
    }

    private static DailySleepLogEntity toEntity(DailySleepLog dailySleepLog) {
        long timeInBedInMinutes = Duration.between(dailySleepLog.getSleepStart(), dailySleepLog.getSleepEnd()).toMinutes();
        return DailySleepLogEntity.builder()
            .userId(dailySleepLog.getUserId())
            .sleepStart(dailySleepLog.getSleepStart())
            .sleepEnd(dailySleepLog.getSleepEnd())
            .sleepDuration(timeInBedInMinutes)
            .sleepQuality(dailySleepLog.getSleepQuality())
            .build();
    }

    private DailySleepLog toDomain(DailySleepLogEntity dailySleepLogEntity) {
        return DailySleepLog.builder()
            .id(dailySleepLogEntity.getId())
            .userId(dailySleepLogEntity.getUserId())
            .sleepStart(dailySleepLogEntity.getSleepStart())
            .sleepEnd(dailySleepLogEntity.getSleepEnd())
            .sleepQuality(dailySleepLogEntity.getSleepQuality())
            .build();
    }
}
