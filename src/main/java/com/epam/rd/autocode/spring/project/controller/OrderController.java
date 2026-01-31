package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.aop.SecurityLoggingEvent;
import com.epam.rd.autocode.spring.project.dto.*;
import com.epam.rd.autocode.spring.project.service.EmployeeService;
import com.epam.rd.autocode.spring.project.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final EmployeeService employeeService;

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('CLIENT', 'EMPLOYEE')")
    public String findMyOrders(Authentication authentication, Model model,
                               @PageableDefault(size = 5) Pageable pageable, OrderFilter orderFilter) {
        String email = authentication.getName();
        Page<OrderDTO> orders;

        if (authentication.getAuthorities().contains( new SimpleGrantedAuthority("ROLE_EMPLOYEE"))) {
            orders = orderService.getOrdersByEmployee(email, pageable, orderFilter);
        } else {
            orders = orderService.getOrdersByClient(email, pageable, orderFilter);
        }
        model.addAttribute("orders", PageResponse.of(orders));
        model.addAttribute("orderFilter", orderFilter);
        model.addAttribute("email", email);
        return "order/my_orders";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    @SecurityLoggingEvent(message = "Orders review requested")
    public String findAll(Model model,@PageableDefault(size=5) Pageable pageable, OrderFilter orderFilter) {
        Page<OrderDTO> orderDTO = orderService.getAllOrders(orderFilter, pageable);
        model.addAttribute("orders", PageResponse.of(orderDTO));
        model.addAttribute("orderFilter", orderFilter);
        model.addAttribute("employees", employeeService.getAllEmployees());
        return "order/orders";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/assign")
    @SecurityLoggingEvent(message = "Order assign submitted")
    public String orderAssign(@RequestParam("orderId") UUID orderId,
                              @RequestParam("employeeEmail") String employeeEmail, Model model) {
        OrderDTO orderDTO = orderService.orderAssign(orderId, employeeEmail);
        model.addAttribute("orderId", orderDTO.getId());
        model.addAttribute("employeeEmail", orderDTO.getEmployeeEmail());
        return "redirect:/orders/all";
    }
    @PreAuthorize("hasRole('EMPLOYEE')")
    @PatchMapping("/updated-status")
    @SecurityLoggingEvent(message = "Order updated submitted")
    public String changeOrderStatus(@RequestParam("orderId") UUID orderId,
                                    @RequestParam("orderStatus") String status, Model model) {
        OrderDTO orderDTO = orderService.changeOrderStatus(orderId, status);
        model.addAttribute("orderId", orderDTO.getId());
        model.addAttribute("orderStatus",  status);
        return "redirect:/orders/my";
    }

    @PreAuthorize("hasRole('CLIENT')")
    @GetMapping("/checkout-confirm")
    public String checkoutConfirmation(Principal principal, Model model ) {
        OrderConfirmation orderConfirmation = orderService.getOrderConfirmation(principal.getName());

        model.addAttribute("cart", orderConfirmation.getCartDTO());
        model.addAttribute("clientBalance", orderConfirmation.getBalance());
        model.addAttribute("canAfford", orderConfirmation.getCanAfford());

        return "order/order_confirmation";
    }

    @PreAuthorize("hasRole('CLIENT')")
    @PostMapping("/checkout")
    @SecurityLoggingEvent(message = "Order checkout submitted")
    public String checkout(Principal principal) {
            orderService.createOrderFromCart(principal.getName());
            return "redirect:/orders/my";
    }

    @PreAuthorize("hasRole('CLIENT')")
    @PatchMapping("/cancel/{id}")
    @SecurityLoggingEvent(message = "Order cancel submitted")
    public String cancel(@PathVariable("id") UUID orderId, Model model) {
        OrderDTO orderDTO =  orderService.cancel(orderId);
        model.addAttribute("order", orderDTO);
        return "redirect:/orders/my";
    }


    @PreAuthorize("hasRole('CLIENT')")
    @GetMapping("/refund/{id}")
    public String refundForm(@PathVariable("id") UUID orderId, Model model) {
        OrderDTO orderDTO =  orderService.getOrderById(orderId);
        model.addAttribute("order", orderDTO);
        return "order/refund";
    }


    @PreAuthorize("hasRole('CLIENT')")
    @PatchMapping("/refund/{id}")
    @SecurityLoggingEvent(message = "Order refund requested")
    public String refund(@PathVariable("id") UUID orderId,
                         Principal principal,
                        @ModelAttribute("order") OrderDTO orderDTO) {
         orderService.refund(orderId, principal.getName());

        return "redirect:/orders/my";
    }

}
