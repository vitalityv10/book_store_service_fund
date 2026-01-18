package com.epam.rd.autocode.spring.project.dto;

import com.epam.rd.autocode.spring.project.model.enums.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookDTO {

    @NotBlank(message = "{validation.book.name.not_blank}")
    @Size(min = 2, max = 100, message = "{validation.book.name.size}")
    private String name;

    @NotBlank(message = "{validation.book.genre.not_blank}")
    private String genre;

    @NotNull(message = "{validation.book.age_group.not_null}")
    private AgeGroup ageGroup;

    @NotNull(message = "{validation.book.price.not_null}")
    @DecimalMin(value = "0.0", inclusive = false, message = "{validation.book.price.min}")
    @Digits(integer = 6, fraction = 2)
    private BigDecimal price;

    @NotNull(message = "{validation.book.publication_date.not_null}")
    @PastOrPresent(message = "{validation.book.publication_date.past}")
    private LocalDate publicationDate;

    @NotBlank(message = "{validation.book.author.not_blank}")
    @Size(max = 50, message = "{validation.book.author.size}")
    private String author;

    @NotNull(message = "{validation.book.pages.not_null}")
    @Min(value = 1, message = "{validation.book.pages.min}")
    @Max(value = 5000, message = "{validation.book.pages.max}")
    private Integer pages;

    @Size(max = 500, message = "{validation.book.characteristics.size}")
    private String characteristics;

    @NotBlank(message = "{validation.book.description.not_blank}")
    @Size(min = 10, max = 2000, message = "{validation.book.description.size}")
    private String description;

    @NotNull(message = "{validation.book.language.not_null}")
    private Language language;
}