package com.minidoodle.schedular.shared.api;

import com.minidoodle.schedular.meeting.application.exception.BookingConflictException;
import com.minidoodle.schedular.meeting.domain.exception.MeetingNotFoundException;
import com.minidoodle.schedular.shared.api.exception.InvalidRequestException;
import com.minidoodle.schedular.slot.domain.exception.SlotNotBookableException;
import com.minidoodle.schedular.slot.domain.exception.SlotNotFoundException;
import com.minidoodle.schedular.slot.domain.exception.SlotNotModifiableException;
import com.minidoodle.schedular.slot.domain.exception.SlotOverlapException;
import com.minidoodle.schedular.user.domain.exception.DuplicateEmailException;
import com.minidoodle.schedular.user.domain.exception.UserNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
            SlotNotFoundException.class,
            MeetingNotFoundException.class,
            UserNotFoundException.class
    })
    ResponseEntity<ErrorResponse> handleNotFound(RuntimeException exception, HttpServletRequest request) {
        return response(HttpStatus.NOT_FOUND, "NOT_FOUND", exception.getMessage(), request);
    }

    @ExceptionHandler({
            SlotOverlapException.class,
            DuplicateEmailException.class,
            IllegalArgumentException.class,
            ConstraintViolationException.class
    })
    ResponseEntity<ErrorResponse> handleUnprocessable(RuntimeException exception, HttpServletRequest request) {
        return response(HttpStatus.UNPROCESSABLE_ENTITY, "VALIDATION_ERROR", exception.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> handleBeanValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        String message = exception.getBindingResult().getAllErrors().stream()
                .map(error -> error instanceof FieldError fieldError
                        ? fieldError.getField() + ": " + error.getDefaultMessage()
                        : error.getDefaultMessage())
                .sorted()
                .collect(Collectors.joining(", "));
        return response(HttpStatus.UNPROCESSABLE_ENTITY, "VALIDATION_ERROR", message, request);
    }

    @ExceptionHandler({
            SlotNotBookableException.class,
            SlotNotModifiableException.class,
            BookingConflictException.class,
            OptimisticLockingFailureException.class
    })
    ResponseEntity<ErrorResponse> handleConflict(RuntimeException exception, HttpServletRequest request) {
        return response(HttpStatus.CONFLICT, "CONFLICT", exception.getMessage(), request);
    }

    @ExceptionHandler({
            InvalidRequestException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class,
            HttpMessageNotReadableException.class,
            HandlerMethodValidationException.class
    })
    ResponseEntity<ErrorResponse> handleBadRequest(Exception exception, HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, "BAD_REQUEST", exception.getMessage(), request);
    }

    private static ResponseEntity<ErrorResponse> response(
            HttpStatus status,
            String code,
            String message,
            HttpServletRequest request
    ) {
        ErrorResponse body = new ErrorResponse(code, message, Instant.now(), request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}
