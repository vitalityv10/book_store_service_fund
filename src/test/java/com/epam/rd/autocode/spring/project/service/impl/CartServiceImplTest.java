package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.CartDTO;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.Book;
import com.epam.rd.autocode.spring.project.model.Cart;
import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.repo.BookRepository;
import com.epam.rd.autocode.spring.project.repo.CartRepository;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import com.epam.rd.autocode.spring.project.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CartServiceImplTest {
    @Autowired
    private CartService cartService;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private ClientRepository clientRepository;

    private final String TEST_EMAIL = "cart.user@example.com";
    private final String BOOK_NAME = "Clean Architecture";
    private Book book;

    @BeforeEach
    void setUp() {
        Client client = new Client();
        client.setEmail(TEST_EMAIL);
        client.setName("John Doe");
        client.setPassword("password");
        clientRepository.save(client);

        book = new Book();
        book.setName(BOOK_NAME);
        book.setPrice(new BigDecimal("100.00"));
        bookRepository.save(book);
    }

    @Test
    void addBookToCart_NewItem_CalculatesPrice() {
        cartService.addBookToCart(TEST_EMAIL, book.getId());

        Cart cart = cartRepository.getCartByClientEmail(TEST_EMAIL).orElseThrow();
        assertEquals(1, cart.getItems().size());
        assertEquals(0, new BigDecimal("100.00").compareTo(cart.getTotalPrice()));
        assertEquals(BOOK_NAME, cart.getItems().get(0).getBook().getName());
    }

    @Test
    void addBookToCart_ExistingItem_IncrementsQuantity() {
        cartService.addBookToCart(TEST_EMAIL, book.getId());

        cartService.addBookToCart(TEST_EMAIL, book.getId());

        Cart cart = cartRepository.getCartByClientEmail(TEST_EMAIL).orElseThrow();
        assertEquals(1, cart.getItems().size());
        assertEquals(2, cart.getItems().get(0).getQuantity());
        assertEquals(0, new BigDecimal("200.00").compareTo(cart.getTotalPrice()));
    }

    @Test
    void getCart_ReturnsDtoWithCorrectData() {
        cartService.addBookToCart(TEST_EMAIL, book.getId());

        CartDTO cartDTO = cartService.getCart(TEST_EMAIL);

        assertNotNull(cartDTO);
        assertEquals(TEST_EMAIL, cartDTO.getClientEmail());
        assertFalse(cartDTO.getItems().isEmpty());
    }

    @Test
    void removeBookFromCart_Success() {
        cartService.addBookToCart(TEST_EMAIL, book.getId());

        cartService.removeBookFromCart(TEST_EMAIL, BOOK_NAME);

        Cart cart = cartRepository.getCartByClientEmail(TEST_EMAIL).orElseThrow();
        assertTrue(cart.getItems().isEmpty());
        assertEquals(0, BigDecimal.ZERO.compareTo(cart.getTotalPrice()));
    }

    @Test
    void updateQuantity_UpdatesTotal() {
        cartService.addBookToCart(TEST_EMAIL, book.getId());

        cartService.updateQuantity(TEST_EMAIL, BOOK_NAME, 5);

        Cart cart = cartRepository.getCartByClientEmail(TEST_EMAIL).orElseThrow();
        assertEquals(5, cart.getItems().get(0).getQuantity());
        assertEquals(0, new BigDecimal("500.00").compareTo(cart.getTotalPrice()));
    }

    @Test
    void updateQuantity_ItemNotFound_ThrowsException() {
        assertThrows(NotFoundException.class, () ->
                cartService.updateQuantity(TEST_EMAIL, "Non Existent Book", 10));
    }
}