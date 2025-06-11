package io.adampoi.java_auto_grader.filter;

import io.adampoi.java_auto_grader.domain.StudentClassroom;
import io.github.acoboh.query.filter.jpa.annotations.QFDefinitionClass;
import io.github.acoboh.query.filter.jpa.annotations.QFElement;
import io.github.acoboh.query.filter.jpa.annotations.QFElements;
import io.github.acoboh.query.filter.jpa.predicate.PredicateOperation;

@QFDefinitionClass(StudentClassroom.class)
public class StudentClassroomFilterDef {

    @QFElement("isActive")
    private String isActive;

    @QFElement("student.id")
    private String student;

    @QFElement("classroom.id")
    private String classroom;

    @QFElements(value = {
            @QFElement("isActive")
    }, operation = PredicateOperation.OR)
    private String search;
}
