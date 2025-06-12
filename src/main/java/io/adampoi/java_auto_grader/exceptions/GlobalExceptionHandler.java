package io.adampoi.java_auto_grader.exceptions;

import io.adampoi.java_auto_grader.model.response.ApiErrorResponse;
import io.adampoi.java_auto_grader.model.response.FieldErrorDetail;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static io.adampoi.java_auto_grader.util.ExceptionUtil.extractFieldName;
import static io.adampoi.java_auto_grader.util.ExceptionUtil.extractRejectedValue;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse.ErrorWrapper> handleAccessDeniedException(
            AccessDeniedException exception, HttpServletRequest request) {
        String message = exception.getMessage().isBlank() || exception.getMessage().equals("Access Denied") ? "You are not authorized to access this resource" : exception.getMessage();
        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                .status(HttpStatus.UNAUTHORIZED.value())
                .message(message)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiErrorResponse.ErrorWrapper(errorResponse));
    }

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
                "Type mismatch: expected " + (exception.getRequiredType() != null ? exception.getRequiredType().getSimpleName() : null),
                exception.getValue() != null ? exception.getValue().toString() : null
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


    @ExceptionHandler({SignatureException.class, ExpiredJwtException.class})
    public ResponseEntity<ApiErrorResponse.ErrorWrapper> handleJwtException(
            Exception exception, HttpServletRequest request) {
        String message = exception instanceof ExpiredJwtException
                ? "The JWT token has expired"
                : "The JWT signature is invalid";

        HttpStatus status = exception instanceof ExpiredJwtException
                ? HttpStatus.UNAUTHORIZED
                : HttpStatus.BAD_REQUEST;

        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                .status(status.value())
                .message(message)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(status)
                .body(new ApiErrorResponse.ErrorWrapper(errorResponse));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse.ErrorWrapper> handleGenericException(
            Exception exception, HttpServletRequest request) {
        log.error("Unknown internal server error at path: {}", request.getRequestURI(), exception);
//        exception.printStackTrace();

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

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse.ErrorWrapper> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException exception, HttpServletRequest request) {

        String supportedMethods = String.join(", ", exception.getSupportedMethods());
        String message = String.format("Request method '%s' not supported. Supported methods are: %s",
                exception.getMethod(), supportedMethods);

        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                .status(HttpStatus.METHOD_NOT_ALLOWED.value())
                .message(message)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(new ApiErrorResponse.ErrorWrapper(errorResponse));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse.ErrorWrapper> handleDataIntegrityViolationException(
            DataIntegrityViolationException exception, HttpServletRequest request) {

        String message = "Data integrity violation";
        List<FieldErrorDetail> fieldErrors = new ArrayList<>();

        if (exception.getCause() != null) {
            String causeMessage = exception.getCause().getMessage().toLowerCase();
            String fieldName = extractFieldName(exception.getCause().getMessage());
            String rejectedValue = extractRejectedValue(exception.getCause().getMessage());
            String errorMessage;

            if (causeMessage.contains("duplicate")) {
                message = "A record with this information already exists";
                errorMessage = String.format("This %s already exists", fieldName);
            } else if (causeMessage.contains("foreign key")) {
                message = "Referenced record does not exist";
                errorMessage = "Referenced record does not exist";
            } else if (causeMessage.contains("not null")) {
                message = "Required field cannot be empty";
                errorMessage = String.format("This %s is required", fieldName);
            } else {
                errorMessage = "Data integrity constraint violated";
            }

            fieldErrors.add(new FieldErrorDetail(
                    fieldName != null ? fieldName : "field",
                    errorMessage,
                    rejectedValue
            ));
        }

        log.warn("Data integrity violation at path: {} - {}", request.getRequestURI(), exception.getMessage());

        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                .status(HttpStatus.CONFLICT.value())
                .message(message)
                .path(request.getRequestURI())
                .fieldErrors(fieldErrors.isEmpty() ? null : fieldErrors)
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiErrorResponse.ErrorWrapper(errorResponse));
    }


    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiErrorResponse.ErrorWrapper> handleDataAccessException(
            DataAccessException exception, HttpServletRequest request) {

        log.error("Database access error at path: {}", request.getRequestURI(), exception);

        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("Database operation failed")
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiErrorResponse.ErrorWrapper(errorResponse));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse.ErrorWrapper> handleNoResourceFoundException(
            NoResourceFoundException exception, HttpServletRequest request) {

        log.error("Resource not found at path: {}", request.getRequestURI(), exception);

        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .message("Resource not found")
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiErrorResponse.ErrorWrapper(errorResponse));
    }

//    @ExceptionHandler(SQLException.class)
//    public ResponseEntity<ApiErrorResponse.ErrorWrapper> handleSQLException(
//            SQLException exception, HttpServletRequest request) {
//
//        log.error("SQL error at path: {} - SQL State: {}, Error Code: {}",
//                request.getRequestURI(), exception.getSQLState(), exception.getErrorCode(), exception);
//
//        String message = "Database operation failed";
//
//        if (exception.getErrorCode() == 1062 || // MySQL duplicate entry
//                exception.getErrorCode() == 23505) { // PostgreSQL unique violation
//            message = "A record with this information already exists";
//        }
//
//        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
//                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
//                .message(message)
//                .path(request.getRequestURI())
//                .build();
//
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                .body(new ApiErrorResponse.ErrorWrapper(errorResponse));
//    }

}