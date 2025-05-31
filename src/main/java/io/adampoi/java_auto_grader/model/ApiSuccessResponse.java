package io.adampoi.java_auto_grader.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.springframework.http.HttpStatusCode;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiSuccessResponse<T> {
    private  T data;
    private  String message;
    private HttpStatusCode statusCode;

}