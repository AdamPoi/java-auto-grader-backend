package io.adampoi.java_auto_grader.filter;

import io.adampoi.java_auto_grader.domain.GradeExecution;
import io.github.acoboh.query.filter.jpa.annotations.QFDefinitionClass;
import io.github.acoboh.query.filter.jpa.annotations.QFElement;
import io.github.acoboh.query.filter.jpa.annotations.QFElements;
import io.github.acoboh.query.filter.jpa.predicate.PredicateOperation;

@QFDefinitionClass(GradeExecution.class)
public class GradeExecutionFilterDef {

    @QFElement("points")
    private String points;

    @QFElement("status")
    private String status;

    @QFElement("actual")
    private String actual;

    @QFElement("expected")
    private String expected;

    @QFElement("error")
    private String error;

    @QFElement("executionTime")
    private String executionTime;

    @QFElement("rubricGrade.id")
    private String rubricGrade;

    @QFElement("submission.id")
    private String submission;

    @QFElements(value = {
            @QFElement("actual"),
            @QFElement("expected"),
            @QFElement("error")
    }, operation = PredicateOperation.OR)
    private String search;
}
