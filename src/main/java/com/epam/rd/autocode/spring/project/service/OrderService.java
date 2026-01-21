package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.OrderConfirmation;
import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.dto.OrderFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.*;

public interface OrderService {

    List<OrderDTO> getOrdersByClient(String clientEmail);

    List<OrderDTO> getOrdersByEmployee(String employeeEmail);

    OrderDTO addOrder(OrderDTO order);

    Page<OrderDTO> getOrdersByClient(String clientEmail, Pageable pageable);
    Page<OrderDTO> getOrdersByEmployee(String employeeEmail , Pageable pageable);

    OrderDTO createOrderFromCart(String name);

    OrderConfirmation getOrderConfirmation(String clientEmail);

    Page<OrderDTO> getAllOrders(OrderFilter orderFilter, Pageable pageable);

    OrderDTO orderAssign(Long orderId, String employeeEmail);

    OrderDTO changeOrderStatus(Long orderId, String status);
}
