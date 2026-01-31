package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.dto.CartDTO;

import java.util.UUID;


public interface CartService {

    void addBookToCart(String clientEmail, UUID bookId);

    CartDTO getCart(String clientEmail);

    void removeBookFromCart(String clientEmail, String bookName);

    void updateQuantity(String email, String bookName, int quantity);
}
