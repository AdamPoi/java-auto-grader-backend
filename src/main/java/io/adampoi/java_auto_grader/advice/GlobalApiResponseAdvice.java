package io.adampoi.java_auto_grader.advice;

import io.adampoi.java_auto_grader.model.ApiErrorResponse;
import io.adampoi.java_auto_grader.model.ApiSuccessResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice(basePackages = "io.adampoi.java_auto_grader.rest")
public class GlobalApiResponseAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return !returnType.getParameterType().isAssignableFrom(ResponseEntity.class);
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {

        if (body instanceof ApiSuccessResponse<?>) {
            ApiSuccessResponse<?> apiResponse = (ApiSuccessResponse<?>) body;
            // Extract status code from ApiSuccessResponse and set it in HTTP response
            if (apiResponse.getStatusCode() != null) {
                response.setStatusCode(HttpStatus.valueOf(apiResponse.getStatusCode().value()));
            }
            // Return the body without the status code
            return ApiSuccessResponse.builder()
                    .data(apiResponse.getData())
                    .message(apiResponse.getMessage())
                    .build();
        }

        if (body instanceof ApiErrorResponse) {
            ApiErrorResponse errorResponse = (ApiErrorResponse) body;
            // Extract status code from ApiErrorResponse and set it in HTTP response
            if (errorResponse.getStatusCode() != null) {
                response.setStatusCode(HttpStatus.valueOf(errorResponse.getStatusCode().value()));
            }
            // Return the body without the status code
            return ApiErrorResponse.builder()
                    .message(errorResponse.getMessage())
                    .fieldErrors(errorResponse.getFieldErrors())
                    .build();
        }

        if (body == null) {
            if (returnType.getParameterType().equals(Void.TYPE) ||
                    returnType.getParameterType().equals(ResponseEntity.class) && body == null) {

                response.setStatusCode(HttpStatus.OK);
                return ApiSuccessResponse.builder()
                        .message("Success")
                        .build();
            }
        }

        response.setStatusCode(HttpStatus.OK);
        return ApiSuccessResponse.builder()
                .data(body)
                .message("Success")
                .build();
    }
}