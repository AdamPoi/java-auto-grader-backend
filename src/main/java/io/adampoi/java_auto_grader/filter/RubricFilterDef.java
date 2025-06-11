package io.adampoi.java_auto_grader.filter;

import io.adampoi.java_auto_grader.domain.Rubric;
import io.github.acoboh.query.filter.jpa.annotations.QFDefinitionClass;
import io.github.acoboh.query.filter.jpa.annotations.QFElement;
import io.github.acoboh.query.filter.jpa.annotations.QFElements;
import io.github.acoboh.query.filter.jpa.predicate.PredicateOperation;

@QFDefinitionClass(Rubric.class)
public class RubricFilterDef {

    @QFElement("name")
    private String name;

    @QFElement("description")
    private String description;

    @QFElement("maxPoints")
    private String maxPoints;

    @QFElement("displayOrder")
    private String displayOrder;

    @QFElement("isActive")
    private String isActive;

    @QFElement("assignment.id")
    private String assignment;

    @QFElements(value = {
            @QFElement("name"),
            @QFElement("description")
    }, operation = PredicateOperation.OR)
    private String search;
}
