package com.noom.interview.backend.sleep.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.noom.interview.backend.sleep.controller.model.response.ErrorResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ResponseBody
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleException(Exception e) {
        log.error(e.getMessage(), e);
        return ErrorResponse.builder()
          .timestamp(LocalDateTime.now())
          .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
          .errors(List.of(e.getMessage()))
          .build();
    }

    @ResponseBody
    @ExceptionHandler(SleepLogAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    public ErrorResponse handleSleepLogAlreadyExistsException(Exception e) {
        log.error(e.getMessage(), e);
        return ErrorResponse.builder()
          .timestamp(LocalDateTime.now())
          .status(HttpStatus.NOT_ACCEPTABLE.value())
          .errors(List.of(e.getMessage()))
          .build();
    }

    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException e, HttpHeaders headers, HttpStatus status, WebRequest request) {
        log.error(e.getMessage(), e);
        List<String> errors = e.getBindingResult().getFieldErrors().stream().map(FieldError::getDefaultMessage).collect(Collectors.toList());

        return new ResponseEntity<>(ErrorResponse.builder()
          .timestamp(LocalDateTime.now())
          .status(status.value())
          .errors(errors)
          .build(), headers, status);
    }

    protected ResponseEntity<Object> handleServletRequestBindingException(ServletRequestBindingException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        log.error(ex.getMessage(), ex);

        return new ResponseEntity<>(ErrorResponse.builder()
          .timestamp(LocalDateTime.now())
          .status(status.value())
          .errors(List.of("Missing required parameter"))
          .build(), headers, status);
    }

    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        log.error(ex.getMessage(), ex);
        String message = "Invalid parameter format";
        if (ex.getCause() instanceof InvalidFormatException) {
            message += ": " + ((InvalidFormatException) ex.getCause()).getValue();

        }
        return new ResponseEntity<>(ErrorResponse.builder()
          .timestamp(LocalDateTime.now())
          .status(status.value())
          .errors(List.of(message))
          .build(), headers, status);
    }
}
