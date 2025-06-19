package io.adampoi.java_auto_grader.filter;

import io.adampoi.java_auto_grader.domain.SubmissionCode;
import io.github.acoboh.query.filter.jpa.annotations.QFDefinitionClass;
import io.github.acoboh.query.filter.jpa.annotations.QFElement;
import io.github.acoboh.query.filter.jpa.annotations.QFElements;
import io.github.acoboh.query.filter.jpa.predicate.PredicateOperation;

@QFDefinitionClass(SubmissionCode.class)
public class SubmissionCodeFilterDef {

    @QFElement("fileName")
    private String fileName;

    @QFElement("sourceCode")
    private String sourceCode;

    @QFElement("className")
    private String className;

    @QFElement("submission.id")
    private String submission;

    @QFElements(value = {
            @QFElement("fileName"),
            @QFElement("className")
    }, operation = PredicateOperation.OR)
    private String search;
}
