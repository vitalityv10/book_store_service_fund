package com.epam.rd.autocode.spring.project.dto;

import com.epam.rd.autocode.spring.project.model.enums.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BookFilter(
         String name,
         String genre,
         AgeGroup ageGroup,
         LocalDate publicationDate,
         String author,
         Integer pages,
        Language language,
         BigDecimal price
){
}
