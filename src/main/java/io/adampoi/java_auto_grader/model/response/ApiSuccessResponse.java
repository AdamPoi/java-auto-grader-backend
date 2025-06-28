package io.adampoi.java_auto_grader.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonView;
import io.adampoi.java_auto_grader.model.dto.Views;
import lombok.*;
import org.springframework.http.HttpStatusCode;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiSuccessResponse<T> {
    @JsonView(Views.External.class)
    private T data;

    @JsonView(Views.External.class)
    private String message;

    @JsonView(Views.External.class)
    private HttpStatusCode statusCode;

}