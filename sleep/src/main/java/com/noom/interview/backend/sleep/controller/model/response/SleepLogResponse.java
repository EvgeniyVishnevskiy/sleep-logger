package com.noom.interview.backend.sleep.controller.model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.noom.interview.backend.sleep.enums.SleepQuality;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SleepLogResponse {

    private Long id;

    private Long userId;

    @JsonFormat(pattern = "MM/dd/yyyy")
    private LocalDate sleepDate;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime sleepStart;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime sleepEnd;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime sleepTime;

    private SleepQuality sleepQuality;

}
