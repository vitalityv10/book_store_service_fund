package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.aop.BusinessLoggingEvent;
import com.epam.rd.autocode.spring.project.dto.*;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.*;
import com.epam.rd.autocode.spring.project.model.enums.OrderStatus;
import com.epam.rd.autocode.spring.project.repo.CartRepository;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import com.epam.rd.autocode.spring.project.repo.EmployeeRepository;
import com.epam.rd.autocode.spring.project.repo.OrderRepository;
import com.epam.rd.autocode.spring.project.service.OrderService;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.modelmapper.ModelMapper;
import org.slf4j.event.Level;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.naming.InsufficientResourcesException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final ClientRepository clientRepository;
    private final CartRepository cartRepository;
    private final EmployeeRepository employeeRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional(readOnly = true)
    public List<OrderDTO> getOrdersByClient(String clientEmail) {
        return orderRepository.getOrdersByClient_Email(clientEmail)
                .stream()
                .map(orderDTO -> modelMapper.map(orderDTO, OrderDTO.class))
                .toList();
    }

    @Override
    public List<OrderDTO> getOrdersByEmployee(String employeeEmail) {
        return  orderRepository.getOrdersByEmployee_Email(employeeEmail)
                .stream()
                .map(orderDTO -> modelMapper.map(orderDTO, OrderDTO.class))
                .toList();
    }

    @Override
    @Transactional
    @BusinessLoggingEvent(message = "Order created")
    public OrderDTO addOrder(OrderDTO orderDTO) {
        return modelMapper.map(orderDTO, OrderDTO.class);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<OrderDTO> getOrdersByClient(String clientEmail, Pageable pageable) {
        return orderRepository.getOrdersByClient_Email(clientEmail, pageable)
                .map(orderDTO -> modelMapper.map(orderDTO, OrderDTO.class));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderDTO> getOrdersByEmployee(String employeeEmail, Pageable pageable) {
        return orderRepository.getOrdersByEmployee_Email(employeeEmail, pageable)
                .map(OrderDTO::toOrderDTO);
    }

    @SneakyThrows
    @Override
    @Transactional
    @BusinessLoggingEvent(message = "Order created from cart")
    public OrderDTO createOrderFromCart(String email) {
        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Client not found"));
        Cart cart = cartRepository.getCartByClientEmail(email)
                .orElseThrow(() -> new NotFoundException("Cart is empty"));

        if (client.getBalance().compareTo(cart.getTotalPrice()) < 0) {
            throw new InsufficientResourcesException("Low balance: " + client.getBalance());
        }

        client.setBalance(client.getBalance().subtract(cart.getTotalPrice()));
        clientRepository.save(client);

        Order savedOrder = orderRepository.save(orderMapper(client, cart));

        cart.getItems().clear();
        cart.setTotalPrice(BigDecimal.ZERO);
        cartRepository.save(cart);

        return modelMapper.map(savedOrder, OrderDTO.class);
    }


    @SneakyThrows
    @Override
    public OrderConfirmation getOrderConfirmation(String clientEmail) {
        Client client = clientRepository.findByEmail(clientEmail)
                .orElseThrow(() -> new NotFoundException("Client not found"));
        Cart cart = cartRepository.getCartByClientEmail(clientEmail)
                .orElseThrow(() -> new NotFoundException("Cart is empty"));

        boolean canAfford = client.getBalance().compareTo(cart.getTotalPrice()) >= 0;
        return new OrderConfirmation(CartDTO.toCartDTO(cart),client.getBalance(), canAfford);
    }

    @Override
    public Page<OrderDTO> getAllOrders(OrderFilter orderFilter, Pageable pageable) {
        QOrder qOrder = QOrder.order;

        BooleanBuilder orderSearch = new BooleanBuilder();
        if (orderFilter.clientEmail() != null && !orderFilter.clientEmail().isBlank()) {
            orderSearch.or(qOrder.client.email.containsIgnoreCase(orderFilter.clientEmail()));
        }
        if (orderFilter.employeeEmail() != null && !orderFilter.employeeEmail().isBlank()) {
            orderSearch.or(qOrder.employee.email.containsIgnoreCase(orderFilter.employeeEmail()));
        }

        Predicate predicate = QPredicates.builder()
                .add(orderFilter.orderDate(), qOrder.orderDate::before)
                .add(orderFilter.price(), qOrder.price::loe)
                .add(orderFilter.orderStatus(), qOrder.orderStatus::eq)
                .add(orderSearch.getValue(), exp -> exp)
                .build();

        return orderRepository.findAll(predicate != null ? predicate : new BooleanBuilder(), pageable)
                .map(OrderDTO::toOrderDTO);
    }

    @Override
    @Transactional
    @BusinessLoggingEvent(message = "Order assigned", value = Level.INFO)
    public OrderDTO orderAssign(Long orderId, String employeeEmail) {
        Order order = orderRepository.getOrdersByIdIs(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));
        Employee employee = employeeRepository.findByEmail(employeeEmail)
                .orElseThrow(() -> new NotFoundException("Employee not found"));
        order.setEmployee(employee);
        order.setOrderStatus(OrderStatus.PROCESSING);
        orderRepository.save(order);
        return modelMapper.map(order, OrderDTO.class);
    }

    @Override
    @Transactional
    @BusinessLoggingEvent(message = "Order status changed", value = Level.INFO)
    public OrderDTO changeOrderStatus(Long orderId, String status) {
        Order order = orderRepository.getOrdersByIdIs(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));
        order.setOrderStatus(OrderStatus.valueOf(status));
        orderRepository.save(order);
        return modelMapper.map(order, OrderDTO.class);
    }


    private static Order orderMapper(Client client, Cart cart) {
        Order orderToSave = new Order();
        orderToSave.setClient(client);
        orderToSave.setPrice(cart.getTotalPrice());
        orderToSave.setOrderDate(LocalDateTime.now());

        List<BookItem> bookItems = toBookItem(cart, orderToSave);

        orderToSave.setBookItems(bookItems);
        return orderToSave;
    }

    private static List<BookItem> toBookItem(Cart cart, Order orderToSave) {
        return cart.getItems().stream()
                .map(cartItem -> {
                    BookItem orderItem = new BookItem();
                    orderItem.setBook(cartItem.getBook());
                    orderItem.setQuantity(cartItem.getQuantity());
                    orderItem.setOrder(orderToSave);
                    return orderItem;
                })
                .toList();
    }
}
