package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.dto.BookFilter;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.Book;
import com.epam.rd.autocode.spring.project.model.enums.AgeGroup;
import com.epam.rd.autocode.spring.project.repo.BookRepository;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BookServiceImplTest {
    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private BookServiceImpl bookService;

    @Test
    void getBookById_ShouldReturnBook() {
        assertEquals("Silent Whispers",  bookService.getBookById(UUID.fromString("b0000000-0000-0000-0000-000000000009")).getName());
    }
    @Test
    void getBookById_ShouldReturnNotFound() {
        assertThrows(NotFoundException.class, () -> bookService.getBookById(UUID.randomUUID()));
    }

    @Test
    void getBookById_ShouldReturnNotFoundWithMessage() {
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            bookService.getBookById(UUID.randomUUID());
        });
        assertTrue(exception.getMessage().contains("Book not found"));
    }

    @Test
    void addBook_Success() {
        Book book = new Book();
        book.setName("The Night in Lisbon");
        book.setAuthor("Erich Maria Remarque");
        book.setAgeGroup(AgeGroup.ADULT);
        book.setPrice(BigDecimal.valueOf(450));

        BookDTO savedBook = bookService.addBook(modelMapper.map(book, BookDTO.class));

        assertNotNull(savedBook.getId());
        assertNotNull(bookService.getBookById(savedBook.getId()));
    }

    @Test
    void addBook_DuplicateName_ShouldThrowException() {
        Book book = new Book();
        book.setId(UUID.fromString("b0000000-0000-0000-0000-000000000009"));
        book.setName("Whispers in the Shadows");
        assertThrows(AlreadyExistException.class,
                () -> bookService.addBook(modelMapper.map(book, BookDTO.class)));
    }

    @Test
    void addBook_LoggingCheck() {
        Book book = new Book();
        book.setName("Unique .");
        book.setAgeGroup(AgeGroup.ADULT);

        bookService.addBook(modelMapper.map(book, BookDTO.class));
    }

    @Test
    void updateBookById_Success() {
        Book book = bookRepository.getBookById(UUID.fromString("b0000000-0000-0000-0000-000000000004")).orElseThrow();
        book.setPrice(BigDecimal.valueOf(999.99));

        bookService.updateBookById(UUID.fromString("b0000000-0000-0000-0000-000000000004"), modelMapper.map(book, BookDTO.class));

        assertEquals(0, new BigDecimal("999.99")
                .compareTo(bookRepository.getBookById(UUID.fromString("b0000000-0000-0000-0000-000000000004")).get().getPrice()));
    }

    @Test
    void updateBookById_NotFound() {
        Book book = new Book();
        assertThrows(NotFoundException.class, () -> bookService.updateBookById(UUID.randomUUID(), modelMapper.map(book, BookDTO.class)));
    }

    @Test
    void updateBookById_ChangeAuthor() {
        Book book = bookRepository.getBookById(UUID.fromString("b0000000-0000-0000-0000-000000000004")).get();
        book.setAuthor("Updated Author");
        bookService.updateBookById(UUID.fromString("b0000000-0000-0000-0000-000000000004"), modelMapper.map(book, BookDTO.class));
        assertEquals("Updated Author",
                bookRepository.getBookById(UUID.fromString("b0000000-0000-0000-0000-000000000004")).get().getAuthor());
    }
    @Test
    void deleteBookById_Success() {
        String name = "b0000000-0000-0000-0000-000000000009";
        bookService.deleteBookById(UUID.fromString(name));
        assertTrue(bookRepository.getBookById(UUID.fromString(name)).isEmpty());
    }

    @Test
    void deleteBookById_NotFound() {
        assertThrows(NotFoundException.class, () -> bookService.deleteBookById(UUID.randomUUID()));
    }

    @Test
    void getAllBooksByFilter() {
        BookFilter bookFilter =new BookFilter(null, null, null, null, null, null, null, null);
        Page<BookDTO> result =  bookService.getBooksByFilter(bookFilter, PageRequest.of(0, 10));
        assertEquals(result.getContent().size(), 10);
    }

    @Test
    void getFIVEBooksByFilter() {
        BookFilter bookFilter =new BookFilter(null, null, null, null, null, null, null, null);
        Page<BookDTO> result =  bookService.getBooksByFilter(bookFilter, PageRequest.of(0, 5));
        assertEquals(result.getContent().size(), 5);
    }


    @Test
    void getBooksByFilter_ShouldReturnBookFromH2() {
        Book book = new Book();
        book.setName("The Black Obelisk");
        book.setAuthor("Erich Maria Remarque");
        bookRepository.save(book);
        BookFilter filter = new BookFilter("The Black Obelisk", null, null, null, "Remarque", null, null, null);
        Pageable pageable = PageRequest.of(0, 5);

        Page<BookDTO> result = bookService.getBooksByFilter(filter, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("The Black Obelisk", result.getContent().get(0).getName());
        bookRepository.delete(book);
    }

    @Test
    void getBooksByFilter_ByAuthorAndGenre_ShouldReturnSpecificBook(){
        BookFilter filter = new BookFilter(null, "Adventure", null, null, "Emily White", null, null, null);
        Pageable pageable = PageRequest.of(0, 5);
        Page<BookDTO> result = bookService.getBooksByFilter(filter, pageable);
        assertNotNull(result);
        assertTrue(result.getContent().stream().anyMatch(book -> book.getName().equals("The Hidden Treasure")));
    }

    @Test
    void getBooksByFilter_ByAuthorAndTitle_ShouldReturnSpecificBook(){
        BookFilter filter = new BookFilter("The Starlight Sonata", null, null, null, "Michael Rose", null, null, null);
        Pageable pageable = PageRequest.of(0, 5);
        Page<BookDTO> result = bookService.getBooksByFilter(filter, pageable);
        assertNotNull(result);
        assertTrue(result.stream().anyMatch(book -> book.getGenre().equals("Romance")));

    }


    @Test
    void getBooksByFilter_ByPrice_ShouldReturnMatchingBooks() {
        BookFilter filter = new BookFilter(null, null, null, null, null, null, null, new BigDecimal("15.99"));
        Pageable pageable = PageRequest.of(0, 5);
        Page<BookDTO> result = bookService.getBooksByFilter(filter, pageable);
        assertNotNull(result);
        assertEquals(0, new BigDecimal("15.99").compareTo(result.getContent().get(0).getPrice()));
    }

    @Test
    void getBooksByFilter_NonExistentAuthor_ShouldReturnEmptyPage() {
        BookFilter filter = new BookFilter(null, null, null, null, "Unknown", null, null, new BigDecimal("15.99"));
        Pageable pageable = PageRequest.of(0, 5);
        Page<BookDTO> result = bookService.getBooksByFilter(filter, pageable);
        //assertNull(result);
        assertEquals(0, result.getTotalElements());
    }

//
//    INSERT INTO books (id, name, genre, age_group, price, publication_year, author, number_of_pages, characteristics, description, language)
//    VALUES
//            ('b0000000-0000-0000-0000-000000000001', 'The Hidden Treasure', 'Adventure', 'ADULT', 24.99, '2018-05-15', 'Emily White', 400, 'Mysterious journey', 'An enthralling adventure of discovery', 'ENGLISH'),
//    ('b0000000-0000-0000-0000-000000000002', 'Echoes of Eternity', 'Fantasy', 'TEEN', 16.50, '2011-01-15', 'Daniel Black', 350, 'Magical realms', 'A spellbinding tale of magic and destiny', 'ENGLISH'),
//            ('b0000000-0000-0000-0000-000000000003', 'Whispers in the Shadows', 'Mystery', 'ADULT', 29.95, '2018-08-11', 'Sophia Green', 450, 'Intriguing suspense', 'A gripping mystery that keeps you guessing', 'ENGLISH'),
//            ('b0000000-0000-0000-0000-000000000004', 'The Starlight Sonata', 'Romance', 'ADULT', 21.75, '2011-05-15', 'Michael Rose', 320, 'Heartwarming love story', 'A beautiful journey of love and passion', 'ENGLISH'),
//            ('b0000000-0000-0000-0000-000000000005', 'Beyond the Horizon', 'Science Fiction', 'CHILD', 18.99, '2004-05-15', 'Alex Carter', 280, 'Interstellar adventure', 'An epic sci-fi adventure beyond the stars', 'ENGLISH'),
//            ('b0000000-0000-0000-0000-000000000006', 'Dancing with Shadows', 'Thriller', 'ADULT', 26.50, '2015-05-15', 'Olivia Smith', 380, 'Suspenseful twists', 'A thrilling tale of danger and intrigue', 'ENGLISH'),
//            ('b0000000-0000-0000-0000-000000000007', 'Voices in the Wind', 'Historical Fiction', 'ADULT', 32.00, '2017-05-15', 'William Turner', 500, 'Rich historical setting', 'A compelling journey through time', 'ENGLISH'),
//            ('b0000000-0000-0000-0000-000000000008', 'Serenade of Souls', 'Fantasy', 'TEEN', 15.99, '2013-05-15', 'Isabella Reed', 330, 'Enchanting realms', 'A magical fantasy filled with wonder', 'ENGLISH'),
//            ('b0000000-0000-0000-0000-000000000009', 'Silent Whispers', 'Mystery', 'ADULT', 27.50, '2021-05-15', 'Benjamin Hall', 420, 'Intricate detective work', 'A mystery that keeps you on the edge', 'ENGLISH'),
//            ('b0000000-0000-0000-0000-000000000010', 'Whirlwind Romance', 'Romance', 'OTHER', 23.25, '2022-05-15', 'Emma Turner', 360, 'Passionate love affair', 'A romance that sweeps you off your feet', 'ENGLISH');

}