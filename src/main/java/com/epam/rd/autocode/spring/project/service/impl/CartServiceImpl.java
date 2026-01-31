package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.aop.BusinessLoggingEvent;
import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.dto.CartDTO;
import com.epam.rd.autocode.spring.project.dto.CartItemDTO;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.Book;
import com.epam.rd.autocode.spring.project.model.Cart;
import com.epam.rd.autocode.spring.project.model.CartItem;
import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.repo.BookRepository;
import com.epam.rd.autocode.spring.project.repo.CartRepository;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import com.epam.rd.autocode.spring.project.service.CartService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.boot.Banner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    private final BookRepository bookRepository;
    private final ClientRepository clientRepository;

    @Override
    @Transactional
    @BusinessLoggingEvent(message = "Book adding to cart")
    public void addBookToCart(String email, UUID bookId) {
        Cart cart = getOriginCart(email);

        Book book = bookRepository.getBookById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getBook().getName().equals(book.getName()))
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
    @Transactional
    public CartDTO getCart(String clientEmail) {
        return cartRepository.getCartByClientEmail(clientEmail)
                .map(CartDTO::toCartDTO)
                .orElseGet(() -> CartDTO.toCartDTO(addCart(clientEmail)));
    }

    private Cart addCart(String clientEmail) {
        Client client = clientRepository.findByEmail(clientEmail)
                .orElseThrow(() -> new NotFoundException("Client not found"));

        Cart cart = new Cart();
        cart.setClient(client);
        cart.setTotalPrice(BigDecimal.ZERO);
        cart.setItems(new ArrayList<>());
        Cart savedCart = cartRepository.save(cart);

        return savedCart;
    }

    @Override
    @Transactional
    @BusinessLoggingEvent(message = "Removing Book from cart")
    public void removeBookFromCart(String clientEmail, String bookName) {
        Cart cart = getOriginCart(clientEmail);

        cart.getItems().removeIf(item -> item.getBook().getName().equals(bookName));
        recalculateTotalPrice(cart);
        cartRepository.save(cart);
    }

    @Transactional
    @Override
    @BusinessLoggingEvent(message = "Updating quantity in cart")
    public void updateQuantity(String email, String bookName, int quantity) {
        Cart cart = getOriginCart(email);

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getBook().getName().equals(bookName))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Item not found"));

        item.setQuantity(quantity);

        BigDecimal newTotal = cart.getItems().stream()
                .map(i -> i.getBook().getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        cart.setTotalPrice(newTotal);
        cartRepository.save(cart);
    }


    private Cart getOriginCart(String email) {
        return cartRepository.getCartByClientEmail(email)
                .orElseGet(() -> addCart(email));
    }

    private void recalculateTotalPrice(Cart cart) {
        BigDecimal total = cart.getItems().stream()
                .map(item -> item.getBook().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        cart.setTotalPrice(total);
    }
}
