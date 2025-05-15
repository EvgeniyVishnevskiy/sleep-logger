package com.noom.interview.backend.sleep.controller;


import com.noom.interview.backend.sleep.controller.model.request.AddSleepLogRequest;
import com.noom.interview.backend.sleep.controller.model.response.AvgSleepLogResponse;
import com.noom.interview.backend.sleep.controller.model.response.SleepLogResponse;
import com.noom.interview.backend.sleep.dto.AvgSleepLog;
import com.noom.interview.backend.sleep.dto.DailySleepLog;
import com.noom.interview.backend.sleep.service.GetLastSleep;
import com.noom.interview.backend.sleep.service.GetLastXDays;
import com.noom.interview.backend.sleep.service.SaveSleepLog;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/sleeplog")
public class SleepLogController {

    private final SaveSleepLog saveSleepLog;
    private final GetLastXDays getLastXDays;
    private final GetLastSleep getLastSleep;

    @PostMapping
    public ResponseEntity<SleepLogResponse> save(@RequestHeader @NotNull Long userId, @RequestBody @Valid AddSleepLogRequest request) {
        DailySleepLog result = saveSleepLog.save(toEntityValidate(userId, request));
        return new ResponseEntity<>(toLogResponseDTO(result), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<SleepLogResponse> lastSleep(@RequestHeader @NotNull Long userId) {
        DailySleepLog result = getLastSleep.execute(userId);
        return new ResponseEntity<>(toLogResponseDTO(result), HttpStatus.OK);
    }

    @GetMapping(path = "/last-x-days")
    public ResponseEntity<AvgSleepLogResponse> lastXDays(@RequestHeader @NotNull Long userId, @RequestHeader @NotNull Long numberOfDays) {
        AvgSleepLog result = getLastXDays.execute(userId, numberOfDays);
        return new ResponseEntity<>(toAvgLogResponseDTO(result), HttpStatus.OK);
    }


    private DailySleepLog toEntityValidate(Long userId, AddSleepLogRequest request) {
        LocalDate sleepDate = Objects.isNull(request.getSleepDate())
            ? LocalDate.now()
            : request.getSleepDate();

        LocalDateTime start = getStartFromDayBefore(request, sleepDate);
        LocalDateTime end = LocalDateTime.of(sleepDate, request.getSleepEnd());

        return DailySleepLog.builder()
            .userId(userId)
            .sleepStart(start)
            .sleepEnd(end)
            .sleepQuality(request.getSleepQuality())
            .build();
    }

    @NotNull
    private static LocalDateTime getStartFromDayBefore(AddSleepLogRequest request, LocalDate sleepDate) {
        return request.getSleepStart().isAfter(request.getSleepEnd())
            ? LocalDateTime.of(sleepDate.minusDays(1), request.getSleepStart())
            : LocalDateTime.of(sleepDate, request.getSleepStart());
    }

    private SleepLogResponse toLogResponseDTO(DailySleepLog result) {
        if(Objects.isNull(result)) {
            return null;
        }
        return SleepLogResponse
            .builder()
            .id(result.getId())
            .userId(result.getUserId())
            .sleepDate(result.getSleepEnd().toLocalDate())
            .sleepStart(result.getSleepStart().toLocalTime())
            .sleepEnd(result.getSleepEnd().toLocalTime())
            .sleepTime(calculateSleepTime(result.getSleepStart().toLocalTime(), result.getSleepEnd().toLocalTime()))
            .sleepQuality(result.getSleepQuality())
            .build();
    }

    private AvgSleepLogResponse toAvgLogResponseDTO(AvgSleepLog result) {
        if(Objects.isNull(result)) {
            return null;
        }

        return AvgSleepLogResponse
            .builder()
            .userId(result.getUserId())
            .observationRangeDateStart(result.getStartDate())
            .observationRangeDateEnd(result.getEndDate())
            .avgSleepStart(result.getAvgSleepStart())
            .avgSleepEnd(result.getAvgSleepEnd())
            .avgSleepTime(calculateSleepTime(result.getAvgSleepStart(), result.getAvgSleepEnd()))
            .sleepQualityCount(result.getSleepQualityCount())
            .build();
    }

    private LocalTime calculateSleepTime(LocalTime sleepStart, LocalTime sleepEnd) {
        return sleepStart.isAfter(sleepEnd)
            ? LocalTime.ofSecondOfDay( Duration.between(sleepStart, LocalTime.MAX).toSeconds() + sleepEnd.toSecondOfDay()+1 )
            : LocalTime.ofSecondOfDay( sleepEnd.toSecondOfDay()+1 - sleepStart.toSecondOfDay() );
    }
}
