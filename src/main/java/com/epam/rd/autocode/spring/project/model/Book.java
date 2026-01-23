package com.epam.rd.autocode.spring.project.model;

import com.epam.rd.autocode.spring.project.model.enums.*;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "books")

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name ="id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name",
            nullable = false,
            unique = true)
    private String name;

    @Column(name = "genre",
            nullable = false)
    private String genre;

    @Enumerated(EnumType.STRING)
    @Column(name = "age_group",
            nullable = false)
    private AgeGroup ageGroup;

    @Column(name = "price",
            nullable = false)
    private BigDecimal price;

    @Column(name = "publication_year",
            nullable = false)
    private LocalDate publicationDate;

    @Column(name = "author",
            nullable = false)
    private String author;

    @Column(name = "number_of_pages",
            nullable = false)
    private Integer pages;

    @Column(name = "characteristics")
    private String characteristics;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "language")
    private Language language;
}

