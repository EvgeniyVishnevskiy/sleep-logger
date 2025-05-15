package com.noom.interview.backend.sleep.controller.model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ErrorResponse {

    @JsonFormat(pattern = "MM/dd/yyyy HH:mm:ss")
    private LocalDateTime timestamp;
    private int status;
    private List<String> errors;
}
