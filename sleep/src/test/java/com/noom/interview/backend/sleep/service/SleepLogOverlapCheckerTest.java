package com.noom.interview.backend.sleep.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.noom.interview.backend.sleep.db.service.DailySleepLogService;
import com.noom.interview.backend.sleep.dto.DailySleepLog;
import com.noom.interview.backend.sleep.exception.SleepLogAlreadyExistsException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SleepLogOverlapCheckerTest {

    @Mock
    private DailySleepLogService mockSleepService;

    @InjectMocks
    private SaveSleepLog overlapChecker;

    private final Long DEFAULT_USER_ID = 1L;
    private final LocalDate TEST_DATE = LocalDate.of(2025, 5, 15); // Use a fixed date for tests

    private DailySleepLog createSleepLog(UUID id, LocalDateTime start, LocalDateTime end) {
        return DailySleepLog.builder()
          .id(id)
          .userId(DEFAULT_USER_ID)
          .sleepStart(start)
          .sleepEnd(end)
          .build();
    }

    private DailySleepLog createSleepLogWithUserId(UUID id, Long userId, LocalDateTime start, LocalDateTime end) {
        return DailySleepLog.builder()
          .id(id)
          .userId(userId)
          .sleepStart(start)
          .sleepEnd(end)
          .build();
    }


    @BeforeEach
    void setUp() {
        // overlapChecker is already initialized with mockSleepService via @InjectMocks
    }

    private void mockServiceResponse(Long userId, LocalDateTime queryStart, LocalDateTime queryEnd, List<DailySleepLog> response) {
        when(mockSleepService.findByUserIdAndInterval(eq(userId), eq(queryStart), eq(queryEnd)))
          .thenReturn(response);
    }

    private void mockServiceResponseAnyRange(Long userId, List<DailySleepLog> response) {
        when(mockSleepService.findByUserIdAndInterval(eq(userId), any(LocalDateTime.class), any(LocalDateTime.class)))
          .thenReturn(response);
    }

    @Test
    void checkOverlaps_noExistingLogs_shouldNotThrowException() {
        LocalDateTime start = LocalDateTime.of(TEST_DATE, LocalTime.of(22, 0));
        LocalDateTime end = LocalDateTime.of(TEST_DATE.plusDays(1), LocalTime.of(6, 0));
        DailySleepLog newLog = createSleepLog(UUID.randomUUID(), start, end);

        mockServiceResponseAnyRange(DEFAULT_USER_ID, Collections.emptyList());

        assertDoesNotThrow(() -> (overlapChecker).checkOverlaps(newLog));
    }

    @Test
    void checkOverlaps_newLogCompletelyBeforeExisting_shouldNotThrowException() {
        LocalDateTime newStart = LocalDateTime.of(TEST_DATE, LocalTime.of(20, 0));
        LocalDateTime newEnd = LocalDateTime.of(TEST_DATE, LocalTime.of(21, 0));
        DailySleepLog newLog = createSleepLog(UUID.randomUUID(), newStart, newEnd);

        LocalDateTime existingStart = LocalDateTime.of(TEST_DATE, LocalTime.of(22, 0));
        LocalDateTime existingEnd = LocalDateTime.of(TEST_DATE.plusDays(1), LocalTime.of(6, 0));
        DailySleepLog existingLog = createSleepLog(UUID.randomUUID(), existingStart, existingEnd);

        mockServiceResponseAnyRange(DEFAULT_USER_ID, List.of(existingLog));

        assertDoesNotThrow(() -> (overlapChecker).checkOverlaps(newLog));
    }

    @Test
    void checkOverlaps_newLogCompletelyAfterExisting_shouldNotThrowException() {
        LocalDateTime newStart = LocalDateTime.of(TEST_DATE.plusDays(1), LocalTime.of(7, 0));
        LocalDateTime newEnd = LocalDateTime.of(TEST_DATE.plusDays(1), LocalTime.of(8, 0));
        DailySleepLog newLog = createSleepLog(UUID.randomUUID(), newStart, newEnd);

        LocalDateTime existingStart = LocalDateTime.of(TEST_DATE, LocalTime.of(22, 0));
        LocalDateTime existingEnd = LocalDateTime.of(TEST_DATE.plusDays(1), LocalTime.of(6, 0));
        DailySleepLog existingLog = createSleepLog(UUID.randomUUID(), existingStart, existingEnd);

        mockServiceResponseAnyRange(DEFAULT_USER_ID, List.of(existingLog));

        assertDoesNotThrow(() -> (overlapChecker).checkOverlaps(newLog));
    }

    // --- Overlap Scenarios (should throw exception) ---

    @Test
    void checkOverlaps_newLogContainedWithinExisting_shouldThrowException() {
        LocalDateTime newStart = LocalDateTime.of(TEST_DATE, LocalTime.of(23, 0)); // 23:00
        LocalDateTime newEnd = LocalDateTime.of(TEST_DATE.plusDays(1), LocalTime.of(1, 0)); // 01:00 next day
        DailySleepLog newLog = createSleepLog(UUID.randomUUID(), newStart, newEnd);

        LocalDateTime existingStart = LocalDateTime.of(TEST_DATE, LocalTime.of(22, 0)); // 22:00
        LocalDateTime existingEnd = LocalDateTime.of(TEST_DATE.plusDays(1), LocalTime.of(6, 0)); // 06:00 next day
        DailySleepLog existingLog = createSleepLog(UUID.randomUUID(), existingStart, existingEnd);

        mockServiceResponseAnyRange(DEFAULT_USER_ID, List.of(existingLog));

        assertThrows(SleepLogAlreadyExistsException.class, () -> (overlapChecker).checkOverlaps(newLog));
    }

    @Test
    void checkOverlaps_existingLogContainedWithinNew_shouldThrowException() {
        LocalDateTime newStart = LocalDateTime.of(TEST_DATE, LocalTime.of(22, 0));
        LocalDateTime newEnd = LocalDateTime.of(TEST_DATE.plusDays(1), LocalTime.of(6, 0));
        DailySleepLog newLog = createSleepLog(UUID.randomUUID(), newStart, newEnd);

        LocalDateTime existingStart = LocalDateTime.of(TEST_DATE, LocalTime.of(23, 0));
        LocalDateTime existingEnd = LocalDateTime.of(TEST_DATE.plusDays(1), LocalTime.of(1, 0));
        DailySleepLog existingLog = createSleepLog(UUID.randomUUID(), existingStart, existingEnd);

        mockServiceResponseAnyRange(DEFAULT_USER_ID, List.of(existingLog));

        assertThrows(SleepLogAlreadyExistsException.class, () -> (overlapChecker).checkOverlaps(newLog));
    }

    @Test
    void checkOverlaps_newLogOverlapsStartOfExisting_shouldThrowException() {
        LocalDateTime newStart = LocalDateTime.of(TEST_DATE, LocalTime.of(21, 0)); // 21:00
        LocalDateTime newEnd = LocalDateTime.of(TEST_DATE, LocalTime.of(22, 30)); // 22:30
        DailySleepLog newLog = createSleepLog(UUID.randomUUID(), newStart, newEnd);

        LocalDateTime existingStart = LocalDateTime.of(TEST_DATE, LocalTime.of(22, 0)); // 22:00
        LocalDateTime existingEnd = LocalDateTime.of(TEST_DATE.plusDays(1), LocalTime.of(6, 0)); // 06:00 next day
        DailySleepLog existingLog = createSleepLog(UUID.randomUUID(), existingStart, existingEnd);

        mockServiceResponseAnyRange(DEFAULT_USER_ID, List.of(existingLog));

        assertThrows(SleepLogAlreadyExistsException.class, () -> (overlapChecker).checkOverlaps(newLog));
    }

    @Test
    void checkOverlaps_newLogOverlapsEndOfExisting_shouldThrowException() {
        LocalDateTime newStart = LocalDateTime.of(TEST_DATE.plusDays(1), LocalTime.of(5, 30)); // 05:30 next day
        LocalDateTime newEnd = LocalDateTime.of(TEST_DATE.plusDays(1), LocalTime.of(7, 0));   // 07:00 next day
        DailySleepLog newLog = createSleepLog(UUID.randomUUID(), newStart, newEnd);

        LocalDateTime existingStart = LocalDateTime.of(TEST_DATE, LocalTime.of(22, 0)); // 22:00
        LocalDateTime existingEnd = LocalDateTime.of(TEST_DATE.plusDays(1), LocalTime.of(6, 0)); // 06:00 next day
        DailySleepLog existingLog = createSleepLog(UUID.randomUUID(), existingStart, existingEnd);

        mockServiceResponseAnyRange(DEFAULT_USER_ID, List.of(existingLog));

        assertThrows(SleepLogAlreadyExistsException.class, () -> (overlapChecker).checkOverlaps(newLog));
    }

    @Test
    void checkOverlaps_newLogStartsAtExistingEnd_shouldThrowException() { // Touching is overlap
        LocalDateTime newStart = LocalDateTime.of(TEST_DATE, LocalTime.of(22, 0));
        LocalDateTime newEnd = LocalDateTime.of(TEST_DATE.plusDays(1), LocalTime.of(1, 0));
        DailySleepLog newLog = createSleepLog(UUID.randomUUID(), newStart, newEnd);

        LocalDateTime existingStart = LocalDateTime.of(TEST_DATE, LocalTime.of(20, 0));
        LocalDateTime existingEnd = LocalDateTime.of(TEST_DATE, LocalTime.of(22, 0)); // Existing ends where new starts
        DailySleepLog existingLog = createSleepLog(UUID.randomUUID(), existingStart, existingEnd);

        mockServiceResponseAnyRange(DEFAULT_USER_ID, List.of(existingLog));

        assertThrows(SleepLogAlreadyExistsException.class, () -> (overlapChecker).checkOverlaps(newLog));
    }

    @Test
    void checkOverlaps_newLogEndsAtExistingStart_shouldThrowException() { // Touching is overlap
        LocalDateTime newStart = LocalDateTime.of(TEST_DATE, LocalTime.of(20, 0));
        LocalDateTime newEnd = LocalDateTime.of(TEST_DATE, LocalTime.of(22, 0)); // New ends where existing starts
        DailySleepLog newLog = createSleepLog(UUID.randomUUID(), newStart, newEnd);

        LocalDateTime existingStart = LocalDateTime.of(TEST_DATE, LocalTime.of(22, 0));
        LocalDateTime existingEnd = LocalDateTime.of(TEST_DATE.plusDays(1), LocalTime.of(1, 0));
        DailySleepLog existingLog = createSleepLog(UUID.randomUUID(), existingStart, existingEnd);

        mockServiceResponseAnyRange(DEFAULT_USER_ID, List.of(existingLog));

        assertThrows(SleepLogAlreadyExistsException.class, () -> (overlapChecker).checkOverlaps(newLog));
    }

    @Test
    void checkOverlaps_newLogIsIdenticalToExisting_differentIds_shouldThrowException() {
        LocalDateTime start = LocalDateTime.of(TEST_DATE, LocalTime.of(22, 0));
        LocalDateTime end = LocalDateTime.of(TEST_DATE.plusDays(1), LocalTime.of(6, 0));
        DailySleepLog newLog = createSleepLog(UUID.randomUUID(), start, end); // Different ID
        DailySleepLog existingLog = createSleepLog(UUID.randomUUID(), start, end); // Different ID

        mockServiceResponseAnyRange(DEFAULT_USER_ID, List.of(existingLog));

        assertThrows(SleepLogAlreadyExistsException.class, () -> (overlapChecker).checkOverlaps(newLog));
    }

    // --- Special Cases ---

    @Test
    void checkOverlaps_updateScenario_sameLogAndId_shouldNotThrowException() {
        UUID logId = UUID.randomUUID();
        LocalDateTime start = LocalDateTime.of(TEST_DATE, LocalTime.of(22, 0));
        LocalDateTime end = LocalDateTime.of(TEST_DATE.plusDays(1), LocalTime.of(6, 0));

        DailySleepLog logToUpdate = createSleepLog(logId, start, end); // This is the "new" log being checked
        DailySleepLog existingLogFromDb = createSleepLog(logId, start, end); // This is what the service returns

        mockServiceResponseAnyRange(DEFAULT_USER_ID, List.of(existingLogFromDb));

        assertDoesNotThrow(() -> (overlapChecker).checkOverlaps(logToUpdate));
    }

    @Test
    void checkOverlaps_updateScenario_timeModifiedButOverlapsOtherLog_shouldThrowException() {
        UUID logIdToUpdate = UUID.randomUUID();
        UUID otherLogId = UUID.randomUUID();

        // Log being updated, its time is modified
        LocalDateTime newStart = LocalDateTime.of(TEST_DATE, LocalTime.of(22, 0)); // 22:00
        LocalDateTime newEnd = LocalDateTime.of(TEST_DATE.plusDays(1), LocalTime.of(1, 0)); // 01:00
        DailySleepLog logToUpdate = createSleepLog(logIdToUpdate, newStart, newEnd);

        // An existing different log in the DB that now overlaps with the updated time
        LocalDateTime otherExistingStart = LocalDateTime.of(TEST_DATE, LocalTime.of(23, 0)); // 23:00
        LocalDateTime otherExistingEnd = LocalDateTime.of(TEST_DATE.plusDays(1), LocalTime.of(2, 0)); // 02:00
        DailySleepLog otherExistingLog = createSleepLog(otherLogId, otherExistingStart, otherExistingEnd);

        // The original state of the log being updated (might or might not be in this query range now)
        // Let's assume service returns only otherExistingLog for simplicity, or both if range is wide
        mockServiceResponseAnyRange(DEFAULT_USER_ID, List.of(otherExistingLog));

        assertThrows(SleepLogAlreadyExistsException.class, () -> (overlapChecker).checkOverlaps(logToUpdate));
    }

    @Test
    void checkOverlaps_newLogWithNullId_overlapsExisting_shouldThrowException() {
        LocalDateTime newStart = LocalDateTime.of(TEST_DATE, LocalTime.of(22, 0));
        LocalDateTime newEnd = LocalDateTime.of(TEST_DATE.plusDays(1), LocalTime.of(1, 0));
        DailySleepLog newLogWithNullId = createSleepLog(null, newStart, newEnd);

        LocalDateTime existingStart = LocalDateTime.of(TEST_DATE, LocalTime.of(23, 0));
        LocalDateTime existingEnd = LocalDateTime.of(TEST_DATE.plusDays(1), LocalTime.of(2, 0));
        DailySleepLog existingLog = createSleepLog(UUID.randomUUID(), existingStart, existingEnd);

        mockServiceResponseAnyRange(DEFAULT_USER_ID, List.of(existingLog));

        assertThrows(SleepLogAlreadyExistsException.class, () -> (overlapChecker).checkOverlaps(newLogWithNullId));
    }

    @Test
    void checkOverlaps_multipleExistingLogs_oneOverlaps_shouldThrowException() {
        LocalDateTime newStart = LocalDateTime.of(TEST_DATE, LocalTime.of(22, 0)); // 22:00
        LocalDateTime newEnd = LocalDateTime.of(TEST_DATE.plusDays(1), LocalTime.of(1, 0)); // 01:00 next day
        DailySleepLog newLog = createSleepLog(UUID.randomUUID(), newStart, newEnd);

        DailySleepLog nonOverlappingLog = createSleepLog(UUID.randomUUID(),
          LocalDateTime.of(TEST_DATE, LocalTime.of(18, 0)),
          LocalDateTime.of(TEST_DATE, LocalTime.of(19, 0)));

        DailySleepLog overlappingLog = createSleepLog(UUID.randomUUID(),
          LocalDateTime.of(TEST_DATE, LocalTime.of(23, 0)),
          LocalDateTime.of(TEST_DATE.plusDays(1), LocalTime.of(2, 0)));

        mockServiceResponseAnyRange(DEFAULT_USER_ID, List.of(nonOverlappingLog, overlappingLog));

        assertThrows(SleepLogAlreadyExistsException.class, () -> (overlapChecker).checkOverlaps(newLog));
    }

    @Test
    void checkOverlaps_multipleExistingLogs_noneOverlap_shouldNotThrowException() {
        LocalDateTime newStart = LocalDateTime.of(TEST_DATE, LocalTime.of(22, 0));
        LocalDateTime newEnd = LocalDateTime.of(TEST_DATE.plusDays(1), LocalTime.of(1, 0));
        DailySleepLog newLog = createSleepLog(UUID.randomUUID(), newStart, newEnd);

        DailySleepLog logBefore = createSleepLog(UUID.randomUUID(),
          LocalDateTime.of(TEST_DATE, LocalTime.of(18, 0)),
          LocalDateTime.of(TEST_DATE, LocalTime.of(19, 0)));

        DailySleepLog logAfter = createSleepLog(UUID.randomUUID(),
          LocalDateTime.of(TEST_DATE.plusDays(1), LocalTime.of(2, 0)),
          LocalDateTime.of(TEST_DATE.plusDays(1), LocalTime.of(3, 0)));

        mockServiceResponseAnyRange(DEFAULT_USER_ID, List.of(logBefore, logAfter));

        assertDoesNotThrow(() -> (overlapChecker).checkOverlaps(newLog));
    }

}