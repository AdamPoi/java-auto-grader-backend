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

    @QFElement("description")
    private String description;

    @QFElement("points")
    private String points;

    @QFElement("displayOrder")
    private String displayOrder;

    @QFElement("code")
    private String code;

    @QFElement("gradeType")
    private String gradeType;

    @QFElement("rubric.id")
    private String rubric;

    @QFElements(value = {
            @QFElement("name"),
            @QFElement("description"),
            @QFElement("code")
    }, operation = PredicateOperation.OR)
    private String search;
}
