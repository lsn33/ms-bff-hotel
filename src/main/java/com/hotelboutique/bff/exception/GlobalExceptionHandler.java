package com.hotelboutique.bff.exception;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpStatusCodeException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpStatusCodeException.class)
    public ResponseEntity<String> handleDownstreamError(HttpStatusCodeException ex) {
        HttpStatusCode status = ex.getStatusCode();
        return ResponseEntity.status(status).body(ex.getResponseBodyAsString());
    }
}