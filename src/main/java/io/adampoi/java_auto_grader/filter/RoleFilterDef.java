package io.adampoi.java_auto_grader.filter;


import io.adampoi.java_auto_grader.domain.Role;
import io.github.acoboh.query.filter.jpa.annotations.QFDefinitionClass;
import io.github.acoboh.query.filter.jpa.annotations.QFElement;
import io.github.acoboh.query.filter.jpa.annotations.QFElements;
import io.github.acoboh.query.filter.jpa.predicate.PredicateOperation;

@QFDefinitionClass(Role.class)
public class RoleFilterDef {

    @QFElement("name")
    private String name;

    @QFElement("rolePermissions.name")
    private String permissions;

    @QFElements(value = {
            @QFElement("name"),

    }, operation = PredicateOperation.OR)
    private String search;
}