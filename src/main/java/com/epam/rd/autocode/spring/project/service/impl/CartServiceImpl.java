package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.aop.BusinessLoggingEvent;
import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.dto.CartDTO;
import com.epam.rd.autocode.spring.project.dto.CartItemDTO;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.Book;
import com.epam.rd.autocode.spring.project.model.Cart;
import com.epam.rd.autocode.spring.project.model.CartItem;
import com.epam.rd.autocode.spring.project.repo.BookRepository;
import com.epam.rd.autocode.spring.project.repo.CartRepository;
import com.epam.rd.autocode.spring.project.service.CartService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.boot.Banner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    private final BookRepository bookRepository;

    @Override
    @Transactional
    public void addBookToCart(String email, String bookName) {
        Cart cart = getOriginCart(email);

        Book book = bookRepository.getBookByName(bookName)
                .orElseThrow(() -> new RuntimeException("Книга не знайдена: "));

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getBook().getName().equals(bookName))
                .findFirst();

        if (existingItem.isPresent()) {
            existingItem.get().setQuantity(existingItem.get().getQuantity() + 1);
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setBook(book);
            newItem.setQuantity(1);
            cart.getItems().add(newItem);
        }

        recalculateTotalPrice(cart);
    }

    @Override
    @Transactional(readOnly = true)
    public CartDTO getCart(String clientEmail) {
        return cartRepository.getCartByClientEmail(clientEmail)
                .map(CartDTO::toCartDTO)
                .orElseThrow(() -> new NotFoundException("Cart is empty"));
    }

    @Override
    @Transactional
    @BusinessLoggingEvent(message = "Removed Book from cart")
    public void removeBookFromCart(String clientEmail, String bookName) {
        Cart cart = getOriginCart(clientEmail);

        cart.getItems().removeIf(item -> item.getBook().getName().equals(bookName));
        recalculateTotalPrice(cart);
        cartRepository.save(cart);
    }

    @Transactional
    @Override
    @BusinessLoggingEvent(message = "Update quantity in cart")
    public void updateQuantity(String email, String bookName, int quantity) {
        Cart cart = getOriginCart(email);

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getBook().getName().equals(bookName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Item not found"));

        item.setQuantity(quantity);

        BigDecimal newTotal = cart.getItems().stream()
                .map(i -> i.getBook().getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        cart.setTotalPrice(newTotal);
        cartRepository.save(cart);
    }


    private Cart getOriginCart(String email) {
        return cartRepository.getCartByClientEmail(email)
                .orElseThrow(() -> new NotFoundException("Cart not found") );
    }

    private void recalculateTotalPrice(Cart cart) {
        BigDecimal total = cart.getItems().stream()
                .map(item -> item.getBook().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        cart.setTotalPrice(total);
    }
}
