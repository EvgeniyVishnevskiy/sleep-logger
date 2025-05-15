package com.noom.interview.backend.sleep.controller.model.response;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.noom.interview.backend.sleep.enums.SleepQuality;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvgSleepLogResponse {

    private Long userId;

    @JsonFormat(pattern = "MM/dd/yyyy")
    private LocalDate observationRangeDateStart;

    @JsonFormat(pattern = "MM/dd/yyyy")
    private LocalDate observationRangeDateEnd;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime avgSleepStart;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime avgSleepEnd;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime avgSleepTime;

    private Map<SleepQuality, Integer> sleepQualityCount;
}
