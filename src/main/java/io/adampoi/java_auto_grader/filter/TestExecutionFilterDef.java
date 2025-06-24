package io.adampoi.java_auto_grader.filter;

import io.adampoi.java_auto_grader.domain.TestExecution;
import io.github.acoboh.query.filter.jpa.annotations.QFDefinitionClass;
import io.github.acoboh.query.filter.jpa.annotations.QFElement;
import io.github.acoboh.query.filter.jpa.annotations.QFElements;
import io.github.acoboh.query.filter.jpa.predicate.PredicateOperation;

@QFDefinitionClass(TestExecution.class)
public class TestExecutionFilterDef {

    @QFElement("methodName")
    private String methodName;

    @QFElement("status")
    private String status;

    @QFElement("output")
    private String output;

    @QFElement("error")
    private String error;

    @QFElement("executionTime")
    private String executionTime;

    @QFElement("rubricGrade.id")
    private String rubricGrade;

    @QFElement("submission.id")
    private String submission;

    @QFElements(value = {
            @QFElement("methodName"),
            @QFElement("output"),
            @QFElement("error")
    }, operation = PredicateOperation.OR)
    private String search;
}
