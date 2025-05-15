package com.noom.interview.backend.sleep.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.noom.interview.backend.sleep.controller.model.request.AddSleepLogRequest;
import com.noom.interview.backend.sleep.dto.AvgSleepLog;
import com.noom.interview.backend.sleep.dto.DailySleepLog;
import com.noom.interview.backend.sleep.enums.SleepQuality;
import com.noom.interview.backend.sleep.exception.GlobalExceptionHandler;
import com.noom.interview.backend.sleep.service.GetLastSleep;
import com.noom.interview.backend.sleep.service.GetLastXDays;
import com.noom.interview.backend.sleep.service.SaveSleepLog;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;


@ExtendWith(MockitoExtension.class)
class SleepLogControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SaveSleepLog saveSleepLog;

    @Mock
    private GetLastXDays getLastXDays;

    @Mock
    private GetLastSleep getLastSleep;

    @InjectMocks
    private SleepLogController sleepLogController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(sleepLogController)
           .setControllerAdvice(new GlobalExceptionHandler())
          .build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    // --- Test Data ---
    private final Long DEFAULT_USER_ID = 1L;
    private final LocalDate DEFAULT_SLEEP_DATE = LocalDate.of(2025, 5, 15);
    private final LocalTime DEFAULT_SLEEP_START = LocalTime.of(22, 0);
    private final LocalTime DEFAULT_SLEEP_END = LocalTime.of(6, 0);
    private final SleepQuality DEFAULT_SLEEP_QUALITY = SleepQuality.GOOD;
    private final UUID DEFAULT_LOG_ID = UUID.randomUUID();

    private AddSleepLogRequest createValidAddSleepLogRequest() {
        return AddSleepLogRequest.builder()
          .sleepDate(DEFAULT_SLEEP_DATE)
          .sleepStart(DEFAULT_SLEEP_START)
          .sleepEnd(DEFAULT_SLEEP_END)
          .sleepQuality(DEFAULT_SLEEP_QUALITY)
          .build();
    }

    private DailySleepLog createDailySleepLog() {
        return DailySleepLog.builder()
          .id(DEFAULT_LOG_ID)
          .userId(DEFAULT_USER_ID)
          .sleepStart(LocalDateTime.of(DEFAULT_SLEEP_DATE.minusDays(1), DEFAULT_SLEEP_START))
          .sleepEnd(LocalDateTime.of(DEFAULT_SLEEP_DATE, DEFAULT_SLEEP_END))
          .sleepQuality(DEFAULT_SLEEP_QUALITY)
          .build();
    }

    private DailySleepLog createDailySleepLogSameDay() {
        return DailySleepLog.builder()
          .id(DEFAULT_LOG_ID)
          .userId(DEFAULT_USER_ID)
          .sleepStart(LocalDateTime.of(DEFAULT_SLEEP_DATE, LocalTime.of(1, 0)))
          .sleepEnd(LocalDateTime.of(DEFAULT_SLEEP_DATE, LocalTime.of(7, 0)))
          .sleepQuality(DEFAULT_SLEEP_QUALITY)
          .build();
    }

    @Test
    void save_whenValidRequest_shouldReturnSleepLogResponse() throws Exception {
        AddSleepLogRequest request = createValidAddSleepLogRequest();
        DailySleepLog savedLog = createDailySleepLog();

        when(saveSleepLog.save(any(DailySleepLog.class))).thenReturn(savedLog);

        mockMvc.perform(post("/v1/sleeplog")
            .header("userId", DEFAULT_USER_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id", is(DEFAULT_LOG_ID.toString())))
          .andExpect(jsonPath("$.userId", is(DEFAULT_USER_ID.intValue())))
          .andExpect(jsonPath("$.sleepDate", is(DEFAULT_SLEEP_DATE.format(DateTimeFormatter.ofPattern("MM/dd/yyyy")))))
          .andExpect(jsonPath("$.sleepStart", is(DEFAULT_SLEEP_START.format(DateTimeFormatter.ofPattern("HH:mm")))))
          .andExpect(jsonPath("$.sleepEnd", is(DEFAULT_SLEEP_END.format(DateTimeFormatter.ofPattern("HH:mm")))))
          .andExpect(jsonPath("$.sleepQuality", is(DEFAULT_SLEEP_QUALITY.name())))
          .andExpect(jsonPath("$.sleepTime", is("08:00")));
    }

    @Test
    void save_whenSleepStartIsAfterSleepEndAndSameDay_shouldCorrectlyCalculateSleepTime() throws Exception {
        AddSleepLogRequest request = AddSleepLogRequest.builder()
          .sleepDate(DEFAULT_SLEEP_DATE)
          .sleepStart(LocalTime.of(23, 0))
          .sleepEnd(LocalTime.of(7, 0))
          .sleepQuality(SleepQuality.GOOD)
          .build();

        DailySleepLog domainLog = DailySleepLog.builder()
          .id(UUID.randomUUID())
          .userId(DEFAULT_USER_ID)
          .sleepStart(LocalDateTime.of(DEFAULT_SLEEP_DATE.minusDays(1), request.getSleepStart()))
          .sleepEnd(LocalDateTime.of(DEFAULT_SLEEP_DATE, request.getSleepEnd()))
          .sleepQuality(request.getSleepQuality())
          .build();

        when(saveSleepLog.save(any(DailySleepLog.class))).thenReturn(domainLog);

        mockMvc.perform(post("/v1/sleeplog")
            .header("userId", DEFAULT_USER_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.sleepStart", is("23:00")))
          .andExpect(jsonPath("$.sleepEnd", is("07:00")))
          .andExpect(jsonPath("$.sleepTime", is("08:00")));
    }


    @Test
    void save_whenSleepStartIsBeforeSleepEndAndSameDay_shouldCorrectlyCalculateSleepTime() throws Exception {
        AddSleepLogRequest request = AddSleepLogRequest.builder()
          .sleepDate(DEFAULT_SLEEP_DATE)
          .sleepStart(LocalTime.of(1, 0))
          .sleepEnd(LocalTime.of(7, 0))
          .sleepQuality(SleepQuality.BAD)
          .build();

        DailySleepLog domainLog = DailySleepLog.builder()
          .id(UUID.randomUUID())
          .userId(DEFAULT_USER_ID)
          .sleepStart(LocalDateTime.of(DEFAULT_SLEEP_DATE, request.getSleepStart()))
          .sleepEnd(LocalDateTime.of(DEFAULT_SLEEP_DATE, request.getSleepEnd()))
          .sleepQuality(request.getSleepQuality())
          .build();

        when(saveSleepLog.save(any(DailySleepLog.class))).thenReturn(domainLog);

        mockMvc.perform(post("/v1/sleeplog")
            .header("userId", DEFAULT_USER_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.sleepStart", is("01:00")))
          .andExpect(jsonPath("$.sleepEnd", is("07:00")))
          .andExpect(jsonPath("$.sleepTime", is("06:00")));
    }


    @Test
    void save_whenSleepDateIsNull_shouldDefaultToTodayAndCorrectlySetSleepStart() throws Exception {
        AddSleepLogRequest request = AddSleepLogRequest.builder()
          .sleepStart(LocalTime.of(23, 0))
          .sleepEnd(LocalTime.of(5, 0))
          .sleepQuality(SleepQuality.BAD)
          .build();

        LocalDate today = LocalDate.now();
        DailySleepLog savedLog = DailySleepLog.builder()
          .id(DEFAULT_LOG_ID)
          .userId(DEFAULT_USER_ID)
          .sleepStart(LocalDateTime.of(today.minusDays(1), request.getSleepStart()))
          .sleepEnd(LocalDateTime.of(today, request.getSleepEnd()))
          .sleepQuality(request.getSleepQuality())
          .build();

        when(saveSleepLog.save(any(DailySleepLog.class))).thenReturn(savedLog);

        mockMvc.perform(post("/v1/sleeplog")
            .header("userId", DEFAULT_USER_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.userId", is(DEFAULT_USER_ID.intValue())))
          .andExpect(jsonPath("$.sleepDate", is(today.format(DateTimeFormatter.ofPattern("MM/dd/yyyy")))))
          .andExpect(jsonPath("$.sleepStart", is(request.getSleepStart().format(DateTimeFormatter.ofPattern("HH:mm")))))
          .andExpect(jsonPath("$.sleepEnd", is(request.getSleepEnd().format(DateTimeFormatter.ofPattern("HH:mm")))))
          .andExpect(jsonPath("$.sleepTime", is("06:00")));
    }

    @Test
    void save_whenUserIdHeaderMissing_shouldReturnBadRequest() throws Exception {
        AddSleepLogRequest request = createValidAddSleepLogRequest();

        mockMvc.perform(post("/v1/sleeplog")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());
    }

    @Test
    void save_whenRequestBodyInvalid_sleepStartNull_shouldReturnBadRequest() throws Exception {
        AddSleepLogRequest request = AddSleepLogRequest.builder()
          .sleepDate(DEFAULT_SLEEP_DATE)
          .sleepEnd(DEFAULT_SLEEP_END)
          .sleepQuality(DEFAULT_SLEEP_QUALITY)
          .build();

        mockMvc.perform(post("/v1/sleeplog")
            .header("userId", DEFAULT_USER_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());
    }

    @Test
    void save_whenRequestBodyInvalid_sleepEndNull_shouldReturnBadRequest() throws Exception {
        AddSleepLogRequest request = AddSleepLogRequest.builder()
          .sleepDate(DEFAULT_SLEEP_DATE)
          .sleepStart(DEFAULT_SLEEP_START)
          .sleepQuality(DEFAULT_SLEEP_QUALITY)
          .build();

        mockMvc.perform(post("/v1/sleeplog")
            .header("userId", DEFAULT_USER_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());
    }

    @Test
    void save_whenRequestBodyInvalid_sleepQualityNull_shouldReturnBadRequest() throws Exception {
        AddSleepLogRequest request = AddSleepLogRequest.builder()
          .sleepDate(DEFAULT_SLEEP_DATE)
          .sleepStart(DEFAULT_SLEEP_START)
          .sleepEnd(DEFAULT_SLEEP_END)
          .build();

        mockMvc.perform(post("/v1/sleeplog")
            .header("userId", DEFAULT_USER_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());
    }

    @Test
    void lastSleep_whenLogExists_shouldReturnSleepLogResponse() throws Exception {
        DailySleepLog lastLog = createDailySleepLogSameDay();
        when(getLastSleep.execute(DEFAULT_USER_ID)).thenReturn(lastLog);

        mockMvc.perform(get("/v1/sleeplog")
            .header("userId", DEFAULT_USER_ID))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id", is(lastLog.getId().toString())))
          .andExpect(jsonPath("$.userId", is(DEFAULT_USER_ID.intValue())))
          .andExpect(jsonPath("$.sleepDate", is(lastLog.getSleepEnd().toLocalDate().format(DateTimeFormatter.ofPattern("MM/dd/yyyy")))))
          .andExpect(jsonPath("$.sleepStart", is(lastLog.getSleepStart().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")))))
          .andExpect(jsonPath("$.sleepEnd", is(lastLog.getSleepEnd().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")))))
          .andExpect(jsonPath("$.sleepQuality", is(lastLog.getSleepQuality().name())))
          .andExpect(jsonPath("$.sleepTime", is("06:00")));
    }

    @Test
    void lastSleep_whenNoLogExists_shouldReturnOkWithNullBody() throws Exception {
        when(getLastSleep.execute(DEFAULT_USER_ID)).thenReturn(null);

        mockMvc.perform(get("/v1/sleeplog")
            .header("userId", DEFAULT_USER_ID))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$").doesNotExist());
    }

    @Test
    void lastSleep_whenUserIdHeaderMissing_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/v1/sleeplog"))
          .andExpect(status().isBadRequest());
    }

    @Test
    void lastXDays_whenDataExists_shouldReturnAvgSleepLogResponse() throws Exception {
        Long numberOfDays = 7L;
        AvgSleepLog avgLog = AvgSleepLog.builder()
          .userId(DEFAULT_USER_ID)
          .startDate(LocalDate.now().minusDays(numberOfDays - 1))
          .endDate(LocalDate.now())
          .avgSleepStart(LocalTime.of(22, 30))
          .avgSleepEnd(LocalTime.of(6, 30))
          .sleepQualityCount(Map.of(SleepQuality.GOOD, 5, SleepQuality.BAD, 2))
          .build();

        when(getLastXDays.execute(DEFAULT_USER_ID, numberOfDays)).thenReturn(avgLog);

        mockMvc.perform(get("/v1/sleeplog/last-x-days")
            .header("userId", DEFAULT_USER_ID)
            .header("numberOfDays", numberOfDays))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.userId", is(DEFAULT_USER_ID.intValue())))
          .andExpect(jsonPath("$.observationRangeDateStart", is(avgLog.getStartDate().format(DateTimeFormatter.ofPattern("MM/dd/yyyy")))))
          .andExpect(jsonPath("$.observationRangeDateEnd", is(avgLog.getEndDate().format(DateTimeFormatter.ofPattern("MM/dd/yyyy")))))
          .andExpect(jsonPath("$.avgSleepStart", is("22:30")))
          .andExpect(jsonPath("$.avgSleepEnd", is("06:30")))
          .andExpect(jsonPath("$.avgSleepTime", is("08:00")))
          .andExpect(jsonPath("$.sleepQualityCount.GOOD", is(5)))
          .andExpect(jsonPath("$.sleepQualityCount.BAD", is(2)));
    }

    @Test
    void lastXDays_whenNoDataExists_shouldReturnOkWithNullBody() throws Exception {
        Long numberOfDays = 3L;
        when(getLastXDays.execute(DEFAULT_USER_ID, numberOfDays)).thenReturn(null);

        mockMvc.perform(get("/v1/sleeplog/last-x-days")
            .header("userId", DEFAULT_USER_ID)
            .header("numberOfDays", numberOfDays))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$").doesNotExist());
    }

    @Test
    void lastXDays_whenUserIdHeaderMissing_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/v1/sleeplog/last-x-days")
            .header("numberOfDays", 7L))
          .andExpect(status().isBadRequest());
    }

    @Test
    void lastXDays_whenNumberOfDaysHeaderMissing_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/v1/sleeplog/last-x-days")
            .header("userId", DEFAULT_USER_ID))
          .andExpect(status().isBadRequest());
    }
}