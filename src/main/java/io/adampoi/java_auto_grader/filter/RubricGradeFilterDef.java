package io.adampoi.java_auto_grader.filter;

import io.adampoi.java_auto_grader.domain.RubricGrade;
import io.github.acoboh.query.filter.jpa.annotations.QFDefinitionClass;
import io.github.acoboh.query.filter.jpa.annotations.QFElement;
import io.github.acoboh.query.filter.jpa.annotations.QFElements;
import io.github.acoboh.query.filter.jpa.predicate.PredicateOperation;

@QFDefinitionClass(RubricGrade.class)
public class RubricGradeFilterDef {

    @QFElement("name")
    private String name;

    @QFElement("gradeType")
    private String gradeType;

    @QFElement("rubric.id")
    private String rubric;

    @QFElement("assignment.id")
    private String assignment;


    @QFElements(value = {
            @QFElement("name"),
    }, operation = PredicateOperation.OR)
    private String search;
}
