package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.CartDTO;
import com.epam.rd.autocode.spring.project.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
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
    public String updateQuantity(@RequestParam String bookName,
                                 @RequestParam int quantity,
                                 Principal principal) {
        String email = principal.getName();
        cartService.updateQuantity(email, bookName, quantity);

        return "redirect:/carts";
    }

    @GetMapping("/add/{bookName}")
    public String addToCart(@PathVariable String bookName, Principal principal) {
        String email = principal.getName();
        cartService.addBookToCart(email, bookName);

        return "redirect:/books";
    }

    @DeleteMapping("/remove")
    public String removeBook(@RequestParam String bookName,
                                 Principal principal) {
        String email = principal.getName();
        cartService.removeBookFromCart(email, bookName);

        return "redirect:/carts";
    }

}
