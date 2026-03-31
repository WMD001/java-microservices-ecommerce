package com.wmd001.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;
import java.util.Map;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = {MethodArgumentNotValidException.class})
    public ResponseEntity<?> methodArgumentNotValid(MethodArgumentNotValidException e) {
        log.error("handler MethodArgumentNotValidException", e);
        List<Map<String, String>> errors = e.getBindingResult().getAllErrors().stream().map((error) -> {
            if (error instanceof FieldError fieldError) {
                return Map.of("field", fieldError.getField(), "message", fieldError.getDefaultMessage());
            } else {
                return Map.of("error", error.getDefaultMessage());
            }
        }).toList();
        return ResponseEntity
                .badRequest()
                .body(Map.of(
                        "status", 400,
                        "message", "Bad Request",
                        "errors", errors));
    }

}
