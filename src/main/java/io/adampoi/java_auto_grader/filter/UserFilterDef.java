package io.adampoi.java_auto_grader.filter;


import io.adampoi.java_auto_grader.domain.User;
import io.github.acoboh.query.filter.jpa.annotations.QFDefinitionClass;
import io.github.acoboh.query.filter.jpa.annotations.QFElement;
import io.github.acoboh.query.filter.jpa.annotations.QFElements;
import io.github.acoboh.query.filter.jpa.predicate.PredicateOperation;

@QFDefinitionClass(User.class)
public class UserFilterDef {

    @QFElement("firstName")
    private String firstName;

    @QFElement("lastName")
    private String lastName;

    @QFElement("email")
    private String email;

    @QFElement("userRoles.name")
    private String roles;

    @QFElement("userRoles.id")
    private String roleId;

    @QFElement("enrolledCourses.id")
    private String course;


    @QFElement("isActive")
    private Boolean isActive;


    @QFElements(value = {
            @QFElement("firstName"),
            @QFElement("lastName"),
            @QFElement("email")
    }, operation = PredicateOperation.OR)
    private String search;
}