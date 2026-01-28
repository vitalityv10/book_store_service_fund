package com.epam.rd.autocode.spring.project.aop;

import org.slf4j.event.Level;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SecurityLoggingEvent {
    String message() default " ";
    Level value() default Level.INFO;
}
