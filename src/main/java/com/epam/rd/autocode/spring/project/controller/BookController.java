package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.dto.BookFilter;
import com.epam.rd.autocode.spring.project.dto.PageResponse;
import com.epam.rd.autocode.spring.project.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController {
    private final BookService bookService;

    @GetMapping
    public String showBooks(Pageable pageable, BookFilter bookFilter, Model model) {
        Page<BookDTO> bookPage = bookService.getBooksByFilter(bookFilter, pageable);

        model.addAttribute("books", PageResponse.of(bookPage));
        model.addAttribute("bookFilter", bookFilter);

        return "book/books";
    }

    @DeleteMapping("/info/delete/{name}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public String deleteBook(@PathVariable("name") String name) {
        bookService.deleteBookByName(name);
        return "redirect:/books";
    }


    @GetMapping("/search")
    public String search(@RequestParam(value = "query", required = false) String query, Model model) {
        if (query != null && !query.isEmpty()) {
            model.addAttribute("books", bookService.getBookByName(query));
        }
        model.addAttribute("query", query);
        return "book/books";
    }
    @GetMapping(value = "/edit/{name}")
    public String showEditBook(@PathVariable("name") String name,  Model model) {
        BookDTO bookDTO = bookService.getBookByName(name);
        model.addAttribute("name", name);
        model.addAttribute("book", bookDTO);
        return "book/book_edit";
    }

    @PatchMapping(value = "/edit/{name}")
    public String editBook(@ModelAttribute("book") @Valid BookDTO bookDTO,
                           BindingResult result,  @PathVariable("name") String name
                           ) {
        if  (result.hasErrors())
            return "book/book_edit";

        BookDTO updatedBook = bookService.updateBookByName(name, bookDTO);

        return "redirect:/books/info/" + updatedBook.getName();

    }

    @GetMapping("/info/{name}")
    public String bookInfo(@PathVariable("name") String name, Model model) {
        BookDTO bookDTO = bookService.getBookByName(name);
        model.addAttribute("book", bookDTO);
        return "book/book_info";
    }


    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @GetMapping(value = "/add")
    public String addBook(Model model){
        model.addAttribute("book", new BookDTO());
        return "book/book_add";
    }

    @PostMapping(value = "/add")
    public String addBook(@ModelAttribute("book") @Valid BookDTO bookDTO,
                          BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "book/book_add";
        }
        model.addAttribute("book", bookDTO);
        bookService.addBook(bookDTO);
        return "redirect:/books/info/" + bookDTO.getName();
    }




}
