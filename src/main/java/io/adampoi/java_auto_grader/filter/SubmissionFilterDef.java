package io.adampoi.java_auto_grader.filter;

import io.adampoi.java_auto_grader.domain.Submission;
import io.github.acoboh.query.filter.jpa.annotations.QFDefinitionClass;
import io.github.acoboh.query.filter.jpa.annotations.QFElement;
import io.github.acoboh.query.filter.jpa.annotations.QFElements;
import io.github.acoboh.query.filter.jpa.predicate.PredicateOperation;

@QFDefinitionClass(Submission.class)
public class SubmissionFilterDef {

//    @QFElement("submissionTime")
//    private String submissionTime;
//
//    @QFElement("attemptNumber")
//    private String attemptNumber;

    @QFElement("status")
    private String status;

    @QFElement("feedback")
    private String feedback;

//    @QFElement("startedAt")
//    private String startedAt;
//
//    @QFElement("completedAt")
//    private String completedAt;



    @QFElement("assignment.id")
    private String assignment;

    @QFElement("student.id")
    private String student;

    @QFElements(value = {
            @QFElement("status"),
    }, operation = PredicateOperation.OR)
    private String search;
}
