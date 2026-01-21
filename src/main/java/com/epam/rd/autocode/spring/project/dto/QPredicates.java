package com.epam.rd.autocode.spring.project.dto;

import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import lombok.*;

import java.util.*;
import java.util.function.Function;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class QPredicates {

    private final List<Predicate> predicates = new ArrayList<>();

    public static QPredicates builder() {return new QPredicates();}

    public <T> QPredicates add(T object, Function<T, Predicate> function) {
        if (object!=null) {
            if(object instanceof String s && s.isBlank()) {
                return this;
            }
            this.predicates.add(function.apply(object));
        }
        return this;
    }

    public Predicate build(){ return ExpressionUtils.allOf(predicates);}

    public Predicate buildOr() { return ExpressionUtils.anyOf(predicates);}

}
