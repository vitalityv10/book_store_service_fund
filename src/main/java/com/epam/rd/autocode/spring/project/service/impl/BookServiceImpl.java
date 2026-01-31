package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.aop.BusinessLoggingEvent;
import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.dto.BookFilter;
import com.epam.rd.autocode.spring.project.dto.QPredicates;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.Book;
import com.epam.rd.autocode.spring.project.model.BookItem;
import com.epam.rd.autocode.spring.project.model.QBook;
import com.epam.rd.autocode.spring.project.model.enums.OrderStatus;
import com.epam.rd.autocode.spring.project.repo.BookItemRepository;
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
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookServiceImpl implements BookService {
    private final BookRepository bookRepository;
    private final ModelMapper modelMapper;
    private final BookItemRepository bookItemRepository;

    @Override
    public BookDTO getBookById(UUID bookId) {
        Book book = bookRepository.getBookById(bookId).orElseThrow(
                () -> new NotFoundException("Book not found")
        );
        return modelMapper.map(book, BookDTO.class);
    }

    @Override
    @Transactional
    @BusinessLoggingEvent(message = "Updating book details")
    public BookDTO updateBookById(UUID bookId, BookDTO book) {
        Book extBook = bookRepository.getBookById(bookId).orElseThrow(
                () -> new NotFoundException("Book not found")
        );
        modelMapper.map(book,extBook);

        Book updatedBook = bookRepository.save(extBook);
        return modelMapper.map(updatedBook, BookDTO.class);
    }

    @Override
    @Transactional
    @BusinessLoggingEvent(message = "Deleting book")
    public void deleteBookById(UUID bookId) {
        Book book = bookRepository.getBookById(bookId).orElseThrow(
                () ->  new NotFoundException("Book not found")
        );
        if (!canBeDeleted(bookId)) {
            throw new IllegalStateException("error.book.in.use");
        }
        bookItemRepository.deleteAllByBook_Id(bookId);
        bookRepository.delete(book);
    }

    @Override
    @Transactional
    @BusinessLoggingEvent(message = "Adding new book")
    public BookDTO addBook(BookDTO book) {
        bookRepository.getBookById(book.getId()).ifPresent(
                b -> { throw new AlreadyExistException("Book already exists"); }
        );
       Book bookToSave = Book.builder()
                .name(book.getName())
                .author(book.getAuthor())
                .price(book.getPrice())
                .genre(book.getGenre())
                .ageGroup(book.getAgeGroup())
                .publicationDate(book.getPublicationDate())
                .characteristics(book.getCharacteristics())
                .description(book.getDescription())
                .pages(book.getPages())
                .language(book.getLanguage())
                .build();
        bookRepository.save(bookToSave);

        return modelMapper.map(bookToSave, BookDTO.class);
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
                .add(bookFilter.publicationDate(), book.publicationDate::before)
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

    public boolean canBeDeleted(UUID bookId) {
        List<OrderStatus> activeStatuses = List.of(OrderStatus.REFUNDED, OrderStatus.DELIVERED);
        return !bookItemRepository
                .existsByBookIdAndOrder_OrderStatusNotIn(bookId, activeStatuses);
    }
}
