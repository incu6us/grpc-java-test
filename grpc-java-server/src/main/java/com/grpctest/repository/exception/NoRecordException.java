package com.grpctest.repository.exception;

public class NoRecordException extends Exception {
    public NoRecordException() {
        super(new Throwable("no record found"));
    }
}
