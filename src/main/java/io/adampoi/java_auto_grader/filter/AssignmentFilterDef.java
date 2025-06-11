package io.adampoi.java_auto_grader.filter;

import io.adampoi.java_auto_grader.domain.Assignment;
import io.github.acoboh.query.filter.jpa.annotations.QFDefinitionClass;
import io.github.acoboh.query.filter.jpa.annotations.QFElement;
import io.github.acoboh.query.filter.jpa.annotations.QFElements;
import io.github.acoboh.query.filter.jpa.predicate.PredicateOperation;

@QFDefinitionClass(Assignment.class)
public class AssignmentFilterDef {

    @QFElement("title")
    private String title;

    @QFElement("description")
    private String description;

//    @QFElement("dueDate")
//    private String dueDate;
//
//    @QFElement("isPublished")
//    private String isPublished;
//
//    @QFElement("maxAttempts")
//    private String maxAttempts;

//    @QFElement("course.id")
//    private String course;
//
//    @QFElement("createdByTeacher.id")
//    private String createdByTeacher;

    @QFElements(value = {
            @QFElement("title"),
    }, operation = PredicateOperation.OR)
    private String search;
}
