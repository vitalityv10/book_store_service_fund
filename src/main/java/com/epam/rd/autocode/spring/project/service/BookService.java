package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.dto.BookFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface BookService {

    BookDTO getBookById(UUID bookId);

    BookDTO updateBookById(UUID bookId, BookDTO book);

    void deleteBookById(UUID bookId);

    BookDTO addBook(BookDTO book);

    Page<BookDTO> getBooksByFilter(BookFilter filter , Pageable pageable);
    boolean canBeDeleted(UUID bookId);
}
