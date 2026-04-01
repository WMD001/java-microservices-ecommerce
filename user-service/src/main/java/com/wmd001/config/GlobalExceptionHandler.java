package com.wmd001.config;

import com.wmd001.common.ApiResult;
import com.wmd001.response.UserServiceResponse;
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
        log.error("handler MethodArgumentNotValidException:", e);
        List<ApiResult.ValidationError> errors = e.getFieldErrors().stream()
                .map(error -> new ApiResult.ValidationError(
                        error.getField(),
                        error.getDefaultMessage(),
                        error.getRejectedValue()
                ))
                .toList();
        UserServiceResponse.ValidationFailed validationFailed = new UserServiceResponse.ValidationFailed(errors);
        return ResponseEntity
                .badRequest()
                .body(validationFailed);
    }

}
