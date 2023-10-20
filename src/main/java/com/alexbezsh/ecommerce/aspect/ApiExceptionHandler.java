package com.alexbezsh.ecommerce.aspect;

import com.alexbezsh.ecommerce.exception.PayPalException;
import com.alexbezsh.ecommerce.exception.ValidationException;
import com.alexbezsh.ecommerce.exception.notfound.NotFoundException;
import com.alexbezsh.ecommerce.model.api.response.ErrorResponse;
import com.paypal.base.rest.PayPalRESTException;
import jakarta.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handle(MethodArgumentNotValidException e) {
        List<String> errors = e.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.toCollection(ArrayList::new));
        e.getBindingResult().getGlobalErrors().stream()
            .map(DefaultMessageSourceResolvable::getDefaultMessage)
            .forEach(errors::add);
        return badRequest(errors);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handle(MethodArgumentTypeMismatchException e) {
        return badRequest(List.of(e.getMessage()));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handle(ConstraintViolationException e) {
        List<String> errors = e.getConstraintViolations().stream()
            .map(v -> v.getPropertyPath() + ": " + v.getMessage()).toList();
        return badRequest(errors);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handle(ValidationException e) {
        return badRequest(List.of(e.getMessage()));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handle(AuthenticationException e) {
        return new ErrorResponse(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handle(AccessDeniedException e) {
        return new ErrorResponse(HttpStatus.FORBIDDEN, "Access denied");
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handle(NotFoundException e) {
        return new ErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handle(PayPalException e) {
        PayPalRESTException cause = e.getCause();
        int responseCode = cause.getResponsecode();
        HttpStatus status = Optional.ofNullable(HttpStatus.resolve(responseCode))
            .orElse(HttpStatus.INTERNAL_SERVER_ERROR);
        ErrorResponse response = new ErrorResponse(status, cause.getMessage());
        return ResponseEntity.status(responseCode)
            .body(response);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handle(Exception e) {
        String message = "Unexpected error. Reason: " + e.getMessage();
        return new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }

    private ErrorResponse badRequest(List<String> errors) {
        String message = String.join("; ", errors);
        return new ErrorResponse(HttpStatus.BAD_REQUEST, message);
    }

}
