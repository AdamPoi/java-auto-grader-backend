package io.adampoi.java_auto_grader.exceptions;

import io.adampoi.java_auto_grader.model.ApiErrorResponse;
import io.adampoi.java_auto_grader.model.FieldErrorDetail;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiErrorResponse.ErrorWrapper> handleEntityNotFoundException(
            EntityNotFoundException exception, HttpServletRequest request) {
        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .message(exception.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiErrorResponse.ErrorWrapper(errorResponse));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse.ErrorWrapper> handleTypeMismatchException(
            MethodArgumentTypeMismatchException exception, HttpServletRequest request) {

        if (exception.getRequiredType() != null &&
                exception.getRequiredType().equals(UUID.class)) {

            ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                    .status(HttpStatus.NOT_FOUND.value())
                    .message("resource not found")
                    .path(request.getRequestURI())
                    .build();

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiErrorResponse.ErrorWrapper(errorResponse));
        }

        List<FieldErrorDetail> fieldErrors = new ArrayList<>();
        fieldErrors.add(new FieldErrorDetail(
                exception.getName(),
                "Type mismatch: expected " + exception.getRequiredType().getSimpleName(),
                exception.getValue().toString()
        ));

        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message("Invalid parameter type")
                .path(request.getRequestURI())
                .fieldErrors(fieldErrors)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorResponse.ErrorWrapper(errorResponse));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse.ErrorWrapper> handleValidationException(
            MethodArgumentNotValidException exception, HttpServletRequest request) {
        List<FieldErrorDetail> fieldErrors = new ArrayList<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.add(new FieldErrorDetail(error.getField(), error.getDefaultMessage(),
                        error.getRejectedValue() != null ? error.getRejectedValue().toString() : null))
        );

        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message("Validation failed")
                .path(request.getRequestURI())
                .fieldErrors(fieldErrors)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorResponse.ErrorWrapper(errorResponse));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErrorResponse.ErrorWrapper> handleBadCredentialsException(
            BadCredentialsException exception, HttpServletRequest request) {
        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                .status(HttpStatus.UNAUTHORIZED.value())
                .message("The username or password is incorrect")
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiErrorResponse.ErrorWrapper(errorResponse));
    }

    @ExceptionHandler(AccountStatusException.class)
    public ResponseEntity<ApiErrorResponse.ErrorWrapper> handleAccountStatusException(
            AccountStatusException exception, HttpServletRequest request) {
        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                .status(HttpStatus.FORBIDDEN.value())
                .message("The account is locked")
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiErrorResponse.ErrorWrapper(errorResponse));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse.ErrorWrapper> handleAccessDeniedException(
            AccessDeniedException exception, HttpServletRequest request) {
        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                .status(HttpStatus.FORBIDDEN.value())
                .message("You are not authorized to access this resource")
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiErrorResponse.ErrorWrapper(errorResponse));
    }

    @ExceptionHandler({SignatureException.class, ExpiredJwtException.class})
    public ResponseEntity<ApiErrorResponse.ErrorWrapper> handleJwtException(
            Exception exception, HttpServletRequest request) {
        String message = exception instanceof ExpiredJwtException
                ? "The JWT token has expired"
                : "The JWT signature is invalid";

        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                .status(HttpStatus.FORBIDDEN.value())
                .message(message)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiErrorResponse.ErrorWrapper(errorResponse));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse.ErrorWrapper> handleGenericException(
            Exception exception, HttpServletRequest request) {
        // TODO send this stack trace to an observability tool
        exception.printStackTrace();

        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("Unknown internal server error")
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiErrorResponse.ErrorWrapper(errorResponse));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse.ErrorWrapper> handleMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException exception, HttpServletRequest request) {

        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                .message("Unsupported Media Type")
                .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity
                .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(new ApiErrorResponse.ErrorWrapper(errorResponse));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse.ErrorWrapper> handleIllegalArgumentException(
            IllegalArgumentException exception, HttpServletRequest request) {
        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message(exception.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorResponse.ErrorWrapper(errorResponse));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse.ErrorWrapper> handleConstraintViolationException(
            ConstraintViolationException exception, HttpServletRequest request) {
        List<FieldErrorDetail> fieldErrors = new ArrayList<>();
        exception.getConstraintViolations().forEach(violation -> {
            String fieldName = violation.getPropertyPath().toString();
            fieldErrors.add(new FieldErrorDetail(
                    fieldName,
                    violation.getMessage(),
                    violation.getInvalidValue() != null ? violation.getInvalidValue().toString() : null
            ));
        });

        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message("Constraint validation failed")
                .path(request.getRequestURI())
                .fieldErrors(fieldErrors)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorResponse.ErrorWrapper(errorResponse));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiErrorResponse.ErrorWrapper> handleMissingParameterException(
            MissingServletRequestParameterException exception, HttpServletRequest request) {
        List<FieldErrorDetail> fieldErrors = new ArrayList<>();
        fieldErrors.add(new FieldErrorDetail(
                exception.getParameterName(),
                "Required parameter is missing",
                null
        ));

        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message("Required parameter missing")
                .path(request.getRequestURI())
                .fieldErrors(fieldErrors)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorResponse.ErrorWrapper(errorResponse));
    }
}