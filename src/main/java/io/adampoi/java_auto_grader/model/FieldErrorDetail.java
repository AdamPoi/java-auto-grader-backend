package io.adampoi.java_auto_grader.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class FieldErrorDetail {
    private String field;
    private String message;
    private Object rejectedValue;
}