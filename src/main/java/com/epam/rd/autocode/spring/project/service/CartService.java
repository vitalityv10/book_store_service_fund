package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.dto.CartDTO;


public interface CartService {

    void addBookToCart(String clientEmail, String bookName);

    CartDTO getCart(String clientEmail);

    void removeBookFromCart(String clientEmail, String bookName);

    void updateQuantity(String email, String bookName, int quantity);
}
