package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.*;
import com.epam.rd.autocode.spring.project.model.enums.*;
import com.epam.rd.autocode.spring.project.service.BookService;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BookControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    private BookDTO sampleBook;
    private Page<BookDTO> bookPage;
    @BeforeEach
    void setUp() {
        sampleBook = new BookDTO();
        sampleBook.setId(UUID.randomUUID());
        sampleBook.setName("Test Book");
        sampleBook.setGenre("Fiction");
        sampleBook.setAgeGroup(AgeGroup.ADULT);
        sampleBook.setPrice(new BigDecimal("25.99"));
        sampleBook.setPublicationDate(LocalDate.now().minusYears(1));
        sampleBook.setAuthor("Test Author");
        sampleBook.setPages(300);
        sampleBook.setCharacteristics("This is a set of valid book characteristics.");
        sampleBook.setDescription("This is a valid and sufficiently long book description.");
        sampleBook.setLanguage(Language.ENGLISH);

        List<BookDTO> bookList = Collections.singletonList(sampleBook);
        bookPage = new PageImpl<>(bookList, PageRequest.of(0, 6), 1);
    }

    @Test
    void showBooks_ShouldReturnViewWithBooks() throws Exception {
        when(bookService.getBooksByFilter(any(BookFilter.class), any(Pageable.class)))
                .thenReturn(bookPage);
        mockMvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(view().name("book/books"))
                .andExpect(model().attributeExists("books"))
                .andExpect(model().attributeExists("bookFilter"));
    }

    @Test
    void showBooks_ShouldReturnViewWithBookByFilter() throws Exception {
        BigDecimal targetPrice = new BigDecimal("300");
        BookFilter filter = new BookFilter(null, null, null, null, null, null, null, targetPrice);

        when(bookService.getBooksByFilter(eq(filter), any(Pageable.class)))
                .thenReturn(bookPage);

        mockMvc.perform(get("/books")
                        .param("price", "300"))
                .andExpect(status().isOk())
                .andExpect(view().name("book/books"))
                .andExpect(model().attribute("bookFilter", is(filter)))
                .andExpect(model().attribute("books", is(PageResponse.of(bookPage))));

        verify(bookService).getBooksByFilter(eq(filter), any(Pageable.class));
    }


    @Test
    void showBooks_ShouldReturnViewWithBookInDesc() throws Exception {
        BigDecimal targetPrice = new BigDecimal("300");
        BookFilter filter = new BookFilter(null, null, null, null, null, null, null, targetPrice);

        when(bookService.getBooksByFilter(eq(filter), any(Pageable.class)))
                .thenReturn(bookPage);
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);

        mockMvc.perform(get("/books")
                        .param("price", "300")
                        .param("sort", "price,desc"))
                .andExpect(status().isOk());

        verify(bookService).getBooksByFilter(eq(filter), captor.capture());
        Pageable pageable = captor.getValue();

        assertNotNull(pageable.getSort().getOrderFor("price"));
        assertEquals(Sort.Direction.DESC, pageable.getSort().getOrderFor("price").getDirection());
    }

    @Test
    void showBooks_ShouldReturnViewWithBookSearchByNameOrAuthor() throws Exception {
        String name = "Test Book";
        String author = "Test Author";
        BookFilter filter = new BookFilter(name, null, null, null, author, null, null, null);

        when(bookService.getBooksByFilter(eq(filter), any(Pageable.class)))
                .thenReturn(bookPage);
        mockMvc.perform((get("/books")
                .param("name", name)
                .param("author", author)))
                .andExpect(status().isOk())
                .andExpect(view().name("book/books"))
                .andExpect(model().attribute("bookFilter", is(filter)))
                .andExpect(model().attributeExists("books"));

        verify(bookService).getBooksByFilter(eq(filter), any(Pageable.class));
    }

    @Test
    void showEditBook_Anonymous_ShouldReturn_Redirection() throws Exception {
        mockMvc.perform(get("/books/edit/{id}", sampleBook.getId()))
                .andExpect(status().is3xxRedirection());
    }
    @Test
    @WithMockUser("CLIENT")
    void showEditBook_Client_ShouldReturn_404() throws Exception {
        mockMvc.perform(get("/books/edit/{id}", sampleBook.getId()))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUser(roles = {"EMPLOYEE", "ADMIN"})
    void showEditBook_ShouldReturnViewToEmployee() throws Exception {
        when(bookService.getBookById(sampleBook.getId())).thenReturn(sampleBook);
        mockMvc.perform(get("/books/edit/{id}", sampleBook.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("book/book_edit"))
                .andExpect(model().attributeExists("book"));
    }
    @Test
    @WithMockUser(roles = {"EMPLOYEE", "ADMIN"})
    void editBook_Success() throws Exception {
        when(bookService.updateBookById(eq(sampleBook.getId()), any(BookDTO.class))).thenReturn(sampleBook);

        mockMvc.perform(patch("/books/edit/{id}", sampleBook.getId())
                        .param("name", "Test Book")
                        .param("author", sampleBook.getAuthor())
                        .param("genre", sampleBook.getGenre())
                        .param("price", "500.00")
                        .param("pages", "300")
                        .param("characteristics", "Valid characteristics")
                        .param("description", sampleBook.getDescription())
                        .param("ageGroup", sampleBook.getAgeGroup().name())
                        .param("language", sampleBook.getLanguage().name())
                        .param("publicationDate", sampleBook.getPublicationDate().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books/info/" + sampleBook.getId()));
        verify(bookService).updateBookById(eq(sampleBook.getId()), any(BookDTO.class));
    }

    @Test
    void showBookInfo_Anonymous_ShouldReturn_Redirection() throws Exception {
        mockMvc.perform(get("/books/info"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(roles = {"EMPLOYEE", "ADMIN", "CLIENT"})
    void showInfoBook_ShouldReturnView() throws Exception {
        when(bookService.getBookById(sampleBook.getId())).thenReturn(sampleBook);
        mockMvc.perform(get("/books/info/{id}", sampleBook.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("book/book_info"))
                .andExpect(model().attributeExists("book"));
    }

    @Test
    void addBook_Anonymous_ShouldReturn_Redirection() throws Exception {
        mockMvc.perform(get("/books/add"))
                .andExpect(status().is3xxRedirection());
    }
    @Test
    @WithMockUser("CLIENT")
    void addBook_Client_ShouldReturn_Redirection() throws Exception {
        mockMvc.perform(get("/books/add"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "EMPLOYEE"})
    void addBook_ShouldReturnView() throws Exception {
        mockMvc.perform(get("/books/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("book/book_add"))
                .andExpect(model().attributeExists("book"));
    }
    @Test
    @WithMockUser(roles = {"ADMIN", "EMPLOYEE"})
    void addBook_Success() throws Exception {
        when(bookService.addBook(any(BookDTO.class))).thenReturn(sampleBook);

        mockMvc.perform(post("/books/add")
                        .param("name", sampleBook.getName())
                        .param("author", sampleBook.getAuthor())
                        .param("genre", sampleBook.getGenre())
                        .param("price", sampleBook.getPrice().toString())
                        .param("ageGroup", sampleBook.getAgeGroup().name())
                        .param("language", sampleBook.getLanguage().name())
                        .param("pages", String.valueOf(sampleBook.getPages()))
                        .param("characteristics", sampleBook.getCharacteristics())
                        .param("description", sampleBook.getDescription())
                        .param("publicationDate", sampleBook.getPublicationDate().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books/info/" + sampleBook.getId()));


        verify(bookService, times(1)).addBook(any(BookDTO.class));
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "EMPLOYEE"})
    void addBook_ValidationError_ShouldReturnForm() throws Exception {
        mockMvc.perform(post("/books/add")
                        .param("name", "")
                        .param("author", sampleBook.getAuthor())
                        .param("price", "-10.00")
                        .param("description", "Short"))
                .andExpect(status().isOk())
                .andExpect(view().name("book/book_add"))
                .andExpect(model().attributeHasFieldErrors("book", "name", "price", "description"));

        verify(bookService, never()).addBook(any(BookDTO.class));
    }


    @Test
    void deleteBook_Anonymous_ShouldReturn_Redirection() throws Exception {
        mockMvc.perform(delete("/books/info/delete/{id}", sampleBook.getId()))
                .andExpect(status().is3xxRedirection());
        verify(bookService, never()).deleteBookById(any(UUID.class));
    }
    @Test
    @WithMockUser(roles ="CLIENT")
    void deleteBook_Client_ShouldReturn_NotFoundPage() throws Exception {
        mockMvc.perform(delete("/books/info/delete/{id}", sampleBook.getId()))

                .andExpect(status().isNotFound());

        verify(bookService, never()).deleteBookById(any(UUID.class));
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "EMPLOYEE"})
    void deleteBook_ShouldReturnView() throws Exception {
      doNothing().when(bookService).deleteBookById(any(UUID.class));

        mockMvc.perform(delete("/books/info/delete/{id}", sampleBook.getId()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"));
        verify(bookService, times(1)).deleteBookById(any(UUID.class));
    }
}