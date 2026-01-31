package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.aop.SecurityLoggingEvent;
import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.dto.BookFilter;
import com.epam.rd.autocode.spring.project.dto.PageResponse;
import com.epam.rd.autocode.spring.project.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController {
    private final BookService bookService;

    @GetMapping
    public String showBooks(@PageableDefault(size = 5) Pageable pageable, BookFilter bookFilter, Model model) {
        Page<BookDTO> bookPage = bookService.getBooksByFilter(bookFilter, pageable);

        model.addAttribute("books", PageResponse.of(bookPage));
        model.addAttribute("bookFilter", bookFilter);

        return "book/books";
    }

    @DeleteMapping("/info/delete/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @SecurityLoggingEvent(message = "Book deletion requested")
    public String deleteBook(@PathVariable("id") UUID id) {
        bookService.deleteBookById(id);
        return "redirect:/books";
    }

    @GetMapping(value = "/edit/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public String showEditBook(@PathVariable("id")UUID id,  Model model) {
        BookDTO bookDTO = bookService.getBookById(id);
        model.addAttribute("id", id);
        model.addAttribute("book", bookDTO);
        return "book/book_edit";
    }

    @PatchMapping(value = "/edit/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @SecurityLoggingEvent(message = "Book update submitted")
    public String editBook(@ModelAttribute("book") @Valid BookDTO bookDTO,
                           BindingResult result,  @PathVariable("id") UUID id) {
        if  (result.hasErrors())
            return "book/book_edit";

        BookDTO updatedBook = bookService.updateBookById(id, bookDTO);
        return "redirect:/books/info/" + updatedBook.getId();
    }

    @GetMapping("/info/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'CLIENT')")
    public String bookInfo(@PathVariable("id") UUID id, Model model) {
        BookDTO bookDTO = bookService.getBookById(id);
        model.addAttribute("book", bookDTO);
        model.addAttribute("canDelete", bookService.canBeDeleted(id));
        return "book/book_info";
    }


    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @GetMapping(value = "/add")
    public String addBook(Model model){
        model.addAttribute("book", new BookDTO());
        return "book/book_add";
    }

    @PostMapping(value = "/add")
    @SecurityLoggingEvent(message = "New book registration")
    public String addBook(@ModelAttribute("book") @Valid BookDTO bookDTO,
                          BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "book/book_add";
        }
        model.addAttribute("book", bookDTO);
        BookDTO savedBook = bookService.addBook(bookDTO);
        return "redirect:/books/info/" + savedBook.getId();
    }

}
