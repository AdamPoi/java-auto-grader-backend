package io.adampoi.java_auto_grader.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Person {

    private Integer id;
    private String firstName;
    private String lastName;
    private int age;
    private Gender gender;
    private String nationality;

    private static enum Gender {
        MALE, FEMALE;
    }
}

