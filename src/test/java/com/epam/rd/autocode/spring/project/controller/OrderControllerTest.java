package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.*;
import com.epam.rd.autocode.spring.project.model.enums.OrderStatus;
import com.epam.rd.autocode.spring.project.service.EmployeeService;
import com.epam.rd.autocode.spring.project.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class OrderControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @MockBean
    private EmployeeService employeeService;

    private OrderDTO order;

    @BeforeEach
    void setUp() {
        BookItemDTO bookItemDTO = BookItemDTO.builder()
                .bookName("Book Name")
                .quantity(2)
                .build();

        order = OrderDTO.builder()
                .id(UUID.randomUUID())
                .price(new BigDecimal("100"))
                .clientEmail("client2@example.com")
                .employeeEmail("worker@example.com")
                .orderStatus(OrderStatus.NEW)
                .bookItems(Collections.singletonList(bookItemDTO))
                .build();
    }


    @Test
    void findMyOrders_Anonymous302() throws Exception {
        mockMvc.perform(get("/orders/my"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(username = "client2@example.com", roles = "CLIENT")
    void findMyOrders_SuccessForClient() throws Exception {
        Page<OrderDTO> page = new PageImpl<>(List.of(order));
        when(orderService.getOrdersByClient(anyString(), any(Pageable.class), any(OrderFilter.class)))
                .thenReturn(page);

        mockMvc.perform(get("/orders/my"))
                .andExpect(status().isOk())
                .andExpect(view().name("order/my_orders"))
                .andExpect(model().attributeExists("orders"))
                .andExpect(model().attribute("email", "client2@example.com"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void findAll_ViewWithAllOrders() throws Exception {
        Page<OrderDTO> page = new PageImpl<>(List.of(order));
        when(orderService.getAllOrders(any(), any(Pageable.class))).thenReturn(page);
        when(employeeService.getAllEmployees()).thenReturn(List.of());

        mockMvc.perform(get("/orders/all"))
                .andExpect(status().isOk())
                .andExpect(view().name("order/orders"))
                .andExpect(model().attributeExists("orders", "orderFilter", "employees"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void findAll_ViewWithOrdersInASC() throws Exception {
        Page<OrderDTO> page = new PageImpl<>(List.of(order));

        when(orderService.getAllOrders(any(), any(Pageable.class))).thenReturn(page);
        when(employeeService.getAllEmployees()).thenReturn(List.of());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);

        mockMvc.perform(get("/orders/all")
                        .param("sort", "price,asc"))
                .andExpect(status().isOk());

        verify(orderService, times(1)).getAllOrders(any(), captor.capture());
        Pageable pageable = captor.getValue();

        assertNotNull(pageable.getSort().getOrderFor("price"));
        assertEquals(Sort.Direction.ASC, pageable.getSort().getOrderFor("price").getDirection());

    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void findAll_Client404() throws Exception {
        mockMvc.perform(get("/orders/all"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void orderAssign_Success() throws Exception {
        when(orderService.orderAssign(any(UUID.class), anyString())).thenReturn(order);

        mockMvc.perform(patch("/orders/assign")
                        .param("orderId", order.getId().toString())
                        .param("employeeEmail", "worker@example.com")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/all"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void orderAssign_EmployeeNotFound() throws Exception {
        when(orderService.orderAssign(any(UUID.class), anyString()))
                .thenThrow(new RuntimeException("Employee not found"));

        mockMvc.perform(patch("/orders/assign")
                        .param("orderId", UUID.randomUUID().toString())
                        .param("employeeEmail", "non-existent@example.com")
                        .with(csrf()))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void changeOrderStatus_Success() throws Exception {
        when(orderService.changeOrderStatus(any(UUID.class), anyString())).thenReturn(order);

        mockMvc.perform(patch("/orders/updated-status")
                        .param("orderId", order.getId().toString())
                        .param("orderStatus", OrderStatus.PROCESSING.name())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/my"));
    }

    @Test
    @WithMockUser(username = "client2@example.com", roles = "CLIENT")
    void checkoutConfirmation_Success() throws Exception {
        CartDTO cartDTO = CartDTO.builder()
                .items(Collections.singletonList(CartItemDTO.builder()
                                .quantity(2)
                                .price(new BigDecimal("100"))
                                .bookName("Book Name")
                        .build()))
                .totalPrice(new BigDecimal("200"))
                .build();

        OrderConfirmation confirmation = OrderConfirmation.builder()
                .cartDTO(cartDTO)
                .balance(new BigDecimal("1000"))
                .canAfford(true)
                .build();

        when(orderService.getOrderConfirmation("client2@example.com")).thenReturn(confirmation);

        mockMvc.perform(get("/orders/checkout-confirm"))
                .andExpect(status().isOk())
                .andExpect(view().name("order/order_confirmation"))
                .andExpect(model().attribute("cart", cartDTO))
                .andExpect(model().attribute("clientBalance", new BigDecimal("1000")))
                .andExpect(model().attribute("canAfford", true));
    }

    @Test
    @WithMockUser(username = "client2@example.com", roles = "CLIENT")
    void checkout_Success() throws Exception {
        mockMvc.perform(post("/orders/checkout")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/my"));

        verify(orderService).createOrderFromCart("client2@example.com");
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void cancel_Success() throws Exception {
        when(orderService.cancel(any(UUID.class))).thenReturn(order);

        mockMvc.perform(patch("/orders/cancel/" + order.getId())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/my"));
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void refundForm_Success() throws Exception {
        when(orderService.getOrderById(any(UUID.class))).thenReturn(order);

        mockMvc.perform(get("/orders/refund/" + order.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("order/refund"))
                .andExpect(model().attribute("order", order));
    }

    @Test
    @WithMockUser(username = "client2@example.com", roles = "CLIENT")
    void refund_Success() throws Exception {
        mockMvc.perform(patch("/orders/refund/" + order.getId())
                        .with(csrf())
                        .flashAttr("order", order))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/my"));

        verify(orderService).refund(eq(order.getId()), eq("client2@example.com"));
    }

    @Test
    @WithMockUser(username = "client2@example.com", roles = "CLIENT")
    void refund_OrderNotFound() throws Exception {
        doThrow(new RuntimeException("Order not found"))
                .when(orderService).refund(any(UUID.class), anyString());

        mockMvc.perform(patch("/orders/refund/" + UUID.randomUUID())
//                        .with(csrf())
                        .flashAttr("order", order))
                .andExpect(status().isInternalServerError());

        verify(orderService, times(1)).refund(any(UUID.class), anyString());
    }
}