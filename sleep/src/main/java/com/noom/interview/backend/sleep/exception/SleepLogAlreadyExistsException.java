package com.noom.interview.backend.sleep.exception;

public class SleepLogAlreadyExistsException extends RuntimeException{

    public SleepLogAlreadyExistsException(String message) {
        super(message);
    }
}
