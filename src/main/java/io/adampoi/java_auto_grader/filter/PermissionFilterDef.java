package io.adampoi.java_auto_grader.filter;


import io.adampoi.java_auto_grader.domain.Permission;
import io.github.acoboh.query.filter.jpa.annotations.QFDefinitionClass;
import io.github.acoboh.query.filter.jpa.annotations.QFElement;
import io.github.acoboh.query.filter.jpa.annotations.QFElements;
import io.github.acoboh.query.filter.jpa.predicate.PredicateOperation;

@QFDefinitionClass(Permission.class)
public class PermissionFilterDef {

    @QFElement("name")
    private String name;


    @QFElements(value = {
            @QFElement("name"),

    }, operation = PredicateOperation.OR)
    private String search;
}