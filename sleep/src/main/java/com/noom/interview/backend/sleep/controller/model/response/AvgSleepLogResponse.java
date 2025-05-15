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
    private LocalDate sleepDateStart;

    @JsonFormat(pattern = "MM/dd/yyyy")
    private LocalDate sleepDateEnd;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime sleepStart;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime sleepEnd;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime sleepTime;

    private Map<SleepQuality, Integer> sleepQualityCount;
}
