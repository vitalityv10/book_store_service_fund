package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.aop.SecurityLoggingEvent;
import com.epam.rd.autocode.spring.project.dto.CartDTO;
import com.epam.rd.autocode.spring.project.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

@Controller
@PreAuthorize("hasRole('CLIENT')")
@RequestMapping("/carts")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @GetMapping
    public String viewCart(Principal principal, Model model) {
        String email = principal.getName();
        CartDTO cart = cartService.getCart(email);

        model.addAttribute("cart", cart);
        return "cart/cart_view";
    }

    @PostMapping("/update")
    @SecurityLoggingEvent(message = "Cart quantity update submitted")
    public String updateQuantity(@RequestParam String bookName,
                                 @RequestParam int quantity,
                                 Principal principal) {
        String email = principal.getName();
        cartService.updateQuantity(email, bookName, quantity);

        return "redirect:/carts";
    }

    @GetMapping("/add/{id}")
    public String addToCart(@PathVariable("id") UUID bookId, Principal principal) {
        String email = principal.getName();
        cartService.addBookToCart(email, bookId);

        return "redirect:/books";
    }

    @DeleteMapping("/remove")
    @SecurityLoggingEvent(message = "Remove book from cart requested")
    public String removeBook(@RequestParam String bookName,
                                 Principal principal) {
        String email = principal.getName();
        cartService.removeBookFromCart(email, bookName);

        return "redirect:/carts";
    }

}
