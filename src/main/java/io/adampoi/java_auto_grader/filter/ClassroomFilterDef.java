package io.adampoi.java_auto_grader.filter;

import io.adampoi.java_auto_grader.domain.Classroom;
import io.github.acoboh.query.filter.jpa.annotations.QFDefinitionClass;
import io.github.acoboh.query.filter.jpa.annotations.QFElement;
import io.github.acoboh.query.filter.jpa.annotations.QFElements;
import io.github.acoboh.query.filter.jpa.predicate.PredicateOperation;

@QFDefinitionClass(Classroom.class)
public class ClassroomFilterDef {

    @QFElement("name")
    private String name;

    @QFElement("isActive")
    private String isActive;
//
//    @QFElement("enrollmentStartDate")
//    private String enrollmentStartDate;
//
//    @QFElement("enrollmentEndDate")
//    private String enrollmentEndDate;

//    @QFElement("course.id")
//    private String course;
//
//    @QFElement("teacher.id")
//    private String teacher;

    @QFElements(value = {
            @QFElement("name")
    }, operation = PredicateOperation.OR)
    private String search;
}
