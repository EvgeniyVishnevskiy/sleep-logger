package com.noom.interview.backend.sleep.controller.model.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.noom.interview.backend.sleep.enums.SleepQuality;
import jakarta.validation.constraints.NotNull;
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
public class AddSleepLogRequest {

    @JsonFormat(pattern = "MM/dd/yyyy")
    private LocalDate sleepDate;

    @JsonFormat(pattern = "HH:mm")
    @NotNull(message = "sleepStart is required")
    private LocalTime sleepStart;

    @JsonFormat(pattern = "HH:mm")
    @NotNull(message = "sleepEnd is required")
    private LocalTime sleepEnd;

    @NotNull(message = "sleepQuality is required")
    private SleepQuality sleepQuality;
}
