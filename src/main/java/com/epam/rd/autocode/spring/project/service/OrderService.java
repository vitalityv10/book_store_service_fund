package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.OrderConfirmation;
import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.dto.OrderFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.*;

public interface OrderService {
    OrderDTO getOrderById(UUID orderId);

    Page<OrderDTO> getOrdersByClient(String clientEmail, Pageable pageable, OrderFilter orderFilter);
    Page<OrderDTO> getOrdersByEmployee(String employeeEmail , Pageable pageable, OrderFilter orderFilter);

    OrderDTO createOrderFromCart(String name);

    OrderConfirmation getOrderConfirmation(String clientEmail);

    Page<OrderDTO> getAllOrders(OrderFilter orderFilter, Pageable pageable);

    OrderDTO orderAssign(UUID orderId, String employeeEmail);

    OrderDTO changeOrderStatus(UUID orderId, String status);

    OrderDTO cancel(UUID orderId);

    OrderDTO refund(UUID orderId, String name);

}
