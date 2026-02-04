package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.*;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CartControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartService cartService;

    private CartDTO cartDTO;
    private ClientDTO clientDTO;

    @BeforeEach
    void setUp() {
        CartItemDTO cartItemDTO = CartItemDTO.builder()
                .bookName("Book 1")
                .price(BigDecimal.valueOf(10.00))
                .quantity(2)
                .build();

        clientDTO = ClientDTO.builder()
                .password("password123")
                .email("client2@example.com")
                .name("Client 2")
                .build();

        cartDTO = CartDTO.builder()
                .clientEmail(clientDTO.getEmail())
                .totalPrice(new BigDecimal("20"))
                .items(Collections.singletonList(cartItemDTO))
                .build();

    }

    @Test
    void viewCart_Anonymous404() throws  Exception {
        mockMvc.perform(get("/carts"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(roles= "CLIENT")
    void viewCart_ViewWithCartItems() throws  Exception {
        when(cartService.getCart(any())).thenReturn(cartDTO);

        mockMvc.perform(get("/carts"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart/cart_view"))
                .andExpect(model().attributeExists("cart"));

        verify(cartService, times(1)).getCart(any());
    }

    @Test
    @WithMockUser(roles= "CLIENT")
    void viewCart_ViewWithCartItemsForClient() throws  Exception {
        when(cartService.getCart(anyString())).thenReturn(cartDTO);

        mockMvc.perform(get("/carts"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart/cart_view"))
                .andExpect(model().attribute("cart", is(cartDTO)));

        assertEquals(cartDTO.getTotalPrice(), cartService.getCart(clientDTO.getEmail()).getTotalPrice());
        verify(cartService, times(1)).getCart(eq(clientDTO.getEmail()));
    }

    @Test
    @WithMockUser(username = "client2@example.com", roles = "CLIENT")
    void updateQuantity_ShouldUpdateAndRedirect() throws Exception {
        String bookName = "Book 1";
        int quantity = 5;

        mockMvc.perform(post("/carts/update")
                        .param("bookName", bookName)
                        .param("quantity", String.valueOf(quantity)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/carts"));

        verify(cartService).updateQuantity("client2@example.com", bookName, quantity);
    }

    @Test
    @WithMockUser(username = "client2@example.com", roles = "CLIENT")
    void addToCart_ShouldAddAndRedirect() throws Exception {
        String bookName = "b0000000-0000-0000-0000-000000000009";

        mockMvc.perform(get("/carts/add/{id}", UUID.fromString(bookName)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"));

        verify(cartService).addBookToCart("client2@example.com", UUID.fromString(bookName));
    }

    @Test
    @WithMockUser(username = "client2@example.com", roles = "CLIENT")
    void addToCart_BookNotExists() throws Exception {
        UUID bookId = UUID.fromString("b0000000-0000-0000-0000-000000000009");

        doThrow(new NotFoundException("Book not found"))
                .when(cartService).addBookToCart(anyString(), eq(bookId));

        mockMvc.perform(get("/carts/add/{id}", bookId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "client2@example.com", roles = "CLIENT")
    void removeBook_ShouldRemoveAndRedirect() throws Exception {
        String bookName = "Book 1";

        mockMvc.perform(delete("/carts/remove")
                        .param("bookName", bookName))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/carts"));

        verify(cartService).removeBookFromCart("client2@example.com", bookName);
    }

    @Test
    @WithMockUser(username = "client2@example.com", roles = "CLIENT")
    void removeBook_BookNotExists() throws Exception {
        String bookName = "Book 2";

        doThrow(new NotFoundException("Book not found"))
                .when(cartService).removeBookFromCart(clientDTO.getEmail(), bookName);

        mockMvc.perform(delete("/carts/remove")
                        .param("bookName", bookName))
                .andExpect(status().isNotFound());
    }
}