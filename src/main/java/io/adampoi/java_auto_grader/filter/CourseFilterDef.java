package io.adampoi.java_auto_grader.filter;

import io.adampoi.java_auto_grader.domain.Course;
import io.github.acoboh.query.filter.jpa.annotations.QFDefinitionClass;
import io.github.acoboh.query.filter.jpa.annotations.QFElement;
import io.github.acoboh.query.filter.jpa.annotations.QFElements;
import io.github.acoboh.query.filter.jpa.predicate.PredicateOperation;

@QFDefinitionClass(Course.class)
public class CourseFilterDef {

    @QFElement("code")
    private String code;

    @QFElement("name")
    private String name;

    @QFElement("description")
    private String description;

    @QFElement("isActive")
    private String isActive;

//    @QFElement("enrollmentStartDate")
//    private String enrollmentStartDate;
//
//    @QFElement("enrollmentEndDate")
//    private String enrollmentEndDate;
//
//    @QFElement("createdByTeacher.id")
//    private String createdByTeacher;


    @QFElement("enrolledUsers.id")
    private String student;


    @QFElements(value = {
            @QFElement("code"),
            @QFElement("name"),
    }, operation = PredicateOperation.OR)
    private String search;
}
