package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.*;
import com.epam.rd.autocode.spring.project.model.*;
import com.epam.rd.autocode.spring.project.model.enums.OrderStatus;
import com.epam.rd.autocode.spring.project.service.OrderService;
import com.epam.rd.autocode.spring.project.repo.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.naming.InsufficientResourcesException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OrderServiceImplTest {

    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private BookRepository bookRepository;

    private Client client;
    private Employee employee;
    private Book book;

    @BeforeEach
    void setUp() {
        client = new Client();
        client.setEmail("customer@test.com");
        client.setBalance(new BigDecimal("1000.00"));
        client.setName("Customer");
        client.setPassword("pass");
        clientRepository.save(client);

        employee = new Employee();
        employee.setEmail("worker@test.com");
        employee.setName("Worker");
        employee.setPassword("pass");
        employeeRepository.save(employee);

        book = new Book();
        book.setName("Clean Code");
        book.setPrice(new BigDecimal("200.00"));
        bookRepository.save(book);
    }

    @Test
    void createOrderFromCart_Success() {
        Cart cart = new Cart();
        cart.setClient(client);
        cart.setTotalPrice(new BigDecimal("200.00"));
        CartItem item = new CartItem();
        item.setCart(cart);
        item.setBook(book);
        item.setQuantity(1);
        cart.setItems(new ArrayList<>(List.of(item)));
        cartRepository.save(cart);

        OrderDTO result = orderService.createOrderFromCart(client.getEmail());

        assertNotNull(result);
        assertEquals(0, new BigDecimal("800.00").compareTo(clientRepository.findByEmail(client.getEmail()).get().getBalance()));
        assertTrue(cartRepository.getCartByClientEmail(client.getEmail()).get().getItems().isEmpty());
    }

    @Test
    void createOrderFromCart_InsufficientFunds_ThrowsException() {
        client.setBalance(new BigDecimal("50.00"));
        clientRepository.save(client);

        Cart cart = new Cart();
        cart.setClient(client);
        cart.setTotalPrice(new BigDecimal("200.00"));
        cartRepository.save(cart);

        Throwable exception = assertThrows(Exception.class, () ->
                orderService.createOrderFromCart(client.getEmail()));

        assertTrue(exception.getCause() instanceof InsufficientResourcesException
                || exception instanceof InsufficientResourcesException);
    }

    @Test
    void orderAssign_Success() {
        Order order = new Order();
        order.setClient(client);
        order.setPrice(new BigDecimal("200"));
        order.setOrderStatus(OrderStatus.NEW);
        order = orderRepository.save(order);

        OrderDTO assignedOrder = orderService.orderAssign(order.getId(), employee.getEmail());

        assertEquals(OrderStatus.PROCESSING, assignedOrder.getOrderStatus());
        assertEquals(employee.getEmail(), assignedOrder.getEmployeeEmail());
    }

    @Test
    void cancel_Success() {
        Order order = new Order();
        order.setClient(client);
        order.setOrderStatus(OrderStatus.NEW);
        order = orderRepository.save(order);

        orderService.cancel(order.getId());

        assertEquals(OrderStatus.CANCELLED, orderRepository.findById(order.getId()).get().getOrderStatus());
    }

    @Test
    void cancel_WrongStatus_ThrowsException() {
        Order order = new Order();
        order.setClient(client);
        order.setOrderStatus(OrderStatus.REFUNDED);
        order = orderRepository.save(order);

        UUID orderId = order.getId();
        assertThrows(IllegalStateException.class, () -> orderService.cancel(orderId));
    }

    @Test
    void refund_Success() {
        Order order = new Order();
        order.setClient(client);
        order.setPrice(new BigDecimal("200.00"));
        order.setOrderStatus(OrderStatus.CANCELLED);
        order = orderRepository.save(order);

        orderService.refund(order.getId(), client.getEmail());

        assertEquals(0, new BigDecimal("1200.00").compareTo(clientRepository.findByEmail(client.getEmail()).get().getBalance()));
        assertEquals(OrderStatus.REFUNDED, orderRepository.findById(order.getId()).get().getOrderStatus());
    }

    @Test
    void getOrderConfirmation_CorrectData() {
        Cart cart = new Cart();
        cart.setClient(client);
        cart.setTotalPrice(new BigDecimal("200.00"));
        cart.setItems(new ArrayList<>());
        cartRepository.save(cart);

        OrderConfirmation confirmation = orderService.getOrderConfirmation(client.getEmail());

        assertNotNull(confirmation);
        assertTrue(confirmation.getCanAfford());
        assertEquals(0, new BigDecimal("1000.00").compareTo(confirmation.getBalance()));
    }

    @Test
    void getAllOrders_FilterByClientEmail() {
        Order order = new Order();
        order.setClient(client);
        order.setPrice(new BigDecimal("100"));
        order.setOrderStatus(OrderStatus.NEW);

        BookItem bookItem = BookItem.builder()
                .book(book)
                .quantity(1)
                .order(order)
                .build();
        order.setBookItems(Collections.singletonList(bookItem));

        orderRepository.save(order);

        OrderFilter filter = new OrderFilter(client.getEmail(), null, null, null, null);
        Page<OrderDTO> result = orderService.getAllOrders(filter, PageRequest.of(0, 10));

        assertFalse(result.isEmpty());
        assertEquals(client.getEmail(), result.getContent().get(0).getClientEmail());
    }
}