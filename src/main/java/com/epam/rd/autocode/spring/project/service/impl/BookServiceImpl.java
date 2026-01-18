package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.aop.BusinessLoggingEvent;
import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.dto.BookFilter;
import com.epam.rd.autocode.spring.project.dto.QPredicates;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.Book;
import com.epam.rd.autocode.spring.project.model.QBook;
import com.epam.rd.autocode.spring.project.repo.BookRepository;
import com.epam.rd.autocode.spring.project.service.BookService;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookServiceImpl implements BookService {
    private final BookRepository bookRepository;
    private final ModelMapper modelMapper;

    @Override
    public Page<BookDTO> getAllBooks(Pageable pageable) {
        return bookRepository.findAll( pageable)
                .map(book -> modelMapper.map(book, BookDTO.class));
    }

    @Override
    public BookDTO getBookByName(String name) {
        Book book = bookRepository.getBookByName(name).orElseThrow(
                () -> new NotFoundException("Book not found")
        );
        return modelMapper.map(book, BookDTO.class);
    }

    @Override
    @Transactional
    @BusinessLoggingEvent(message = "updating book")
    public BookDTO updateBookByName(String name, BookDTO book) {
        Book extBook = bookRepository.getBookByName(name).orElseThrow(
                () -> new NotFoundException("Book not found")
        );
        modelMapper.map(book,extBook);

        Book updatedBook = bookRepository.save(extBook);
        return modelMapper.map(updatedBook, BookDTO.class);
    }

    @Override
    @Transactional
    @BusinessLoggingEvent(message = "deleting book")
    public void deleteBookByName(String name) {
        Book book = bookRepository.getBookByName(name).orElseThrow(
                () ->  new NotFoundException("Book not found")
        );
        bookRepository.delete(book);
    }

    @Override
    @Transactional
    @BusinessLoggingEvent(message = "Adding new book")
    public BookDTO addBook(BookDTO book) {
        Book updatedBook = bookRepository.save(modelMapper.map(book, Book.class));
        return modelMapper.map(updatedBook, BookDTO.class);
    }

    @Override
    @BusinessLoggingEvent(message = "Books by filter")
    public Page<BookDTO> getBooksByFilter(BookFilter bookFilter, Pageable pageable) {
        QBook book = QBook.book;

        BooleanExpression searchExpression = getBooleanExpression(bookFilter, book);
        Predicate predicate = getPredicate(bookFilter, searchExpression, book);

        Predicate finalPredicate = (predicate != null)? predicate:new BooleanBuilder();
        return bookRepository
                .findAll(finalPredicate, pageable)
                .map(book1 ->  modelMapper.map(book1, BookDTO.class));
    }

    private static Predicate getPredicate(BookFilter bookFilter, BooleanExpression searchExpression, QBook book) {
        Predicate predicate = QPredicates.builder()
                .add(searchExpression, exp->exp)
                .add(bookFilter.genre(), book.genre::eq)
                .add(bookFilter.ageGroup(), book.ageGroup::eq)
                .add(bookFilter.publicationDate(), book.publicationDate::eq)
                .add(bookFilter.author(), book.author::containsIgnoreCase)
                .add(bookFilter.pages(), book.pages::loe)
                .add(bookFilter.language(), book.language::eq)
                .add(bookFilter.price(), book.price::eq)
                .build();
        return predicate;
    }

    private static BooleanExpression getBooleanExpression(BookFilter bookFilter, QBook book) {
        BooleanExpression searchExpression = null;
        if (bookFilter.name() != null && !bookFilter.name().isBlank()) {
            searchExpression = book.name.containsIgnoreCase(bookFilter.name())
                    .or(book.author.containsIgnoreCase(bookFilter.name()));
        }
        return searchExpression;
    }
}
