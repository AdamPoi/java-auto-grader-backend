package io.adampoi.java_auto_grader.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.springframework.http.HttpStatusCode;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiErrorResponse {
    private int status;
    private String message;
    private String path;
    private HttpStatusCode statusCode;
    private List<FieldErrorDetail> fieldErrors;

    public static class ErrorWrapper {
        private ApiErrorResponse error;

        public ErrorWrapper(ApiErrorResponse error) {
            this.error = error;
        }

        public ApiErrorResponse getError() {
            return error;
        }
    }
}