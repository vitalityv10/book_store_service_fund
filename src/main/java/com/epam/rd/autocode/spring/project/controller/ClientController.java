package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.dto.ClientFilter;
import com.epam.rd.autocode.spring.project.dto.topUp.ClientTopUpRequest;
import com.epam.rd.autocode.spring.project.dto.PageResponse;
import com.epam.rd.autocode.spring.project.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequestMapping("/clients")
@RequiredArgsConstructor
public class ClientController {
  private final ClientService clientService;

    @GetMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE')")
    public String getAllClients(ClientFilter clientFilter, @PageableDefault(size = 5) Pageable pageable, Model model) {
        Page<ClientFilter> clientPage = clientService.getClientsByFilter(clientFilter, pageable);
        model.addAttribute("clients", PageResponse.of(clientPage));
        model.addAttribute("clientFilter", clientFilter);
        return "client/clients";
    }

    @GetMapping("/block")
    @PreAuthorize("hasAnyRole('EMPLOYEE')")
    public String blockUnblockClient(@RequestParam("block") Boolean block,
                                     @RequestParam("email") String email) {
        if (Boolean.TRUE.equals(block)) {
            clientService.blockClient(email);
        } else {
            clientService.unblockClient(email);
        }
        return "redirect:/clients";
    }

    @GetMapping("/account/{email}")
    public String showMyAccount(@PathVariable("email") String email, Model model) {
        ClientDTO clientDTO = clientService.getClientByEmail(email);
        model.addAttribute("client", clientDTO);
        return "client/client_account_info";
    }

    @GetMapping("/account/edit/{email}")
    public String showEditForm(@PathVariable("email") String email, Model model) {
        ClientDTO clientDTO = clientService.getClientByEmail(email);
        model.addAttribute("client", clientDTO);
        return "client/client_account_edit";
    }

    @PatchMapping("/account/edit/{email}")
    public String editMyAccount(@ModelAttribute("client")@Valid ClientDTO clientDTO,
                                BindingResult bindingResult,
                                @PathVariable("email") String email){
        if (bindingResult.hasErrors()) {
            return "client/client_account_edit";
        }
        clientService.updateClientByEmail(email, clientDTO);
        return "redirect:/clients/account/" + clientDTO.getEmail();
    }

    @DeleteMapping("/account/delete/{email}")
    public String deleteMyAccount(@PathVariable("email") String email){
        clientService.deleteClientByEmail(email);
        return "redirect:/books";
    }

    @GetMapping("/account/topUp")
    public String showTopUp(Principal principal, Model model) {
        ClientDTO clientDTO = clientService.getClientByEmail(principal.getName());
        model.addAttribute("client", clientDTO);
        return "client/client_top_up";
    }

    @PatchMapping("/account/topUp")
    public String topUpMyAccount(Principal principal,
                                 @ModelAttribute("client") ClientTopUpRequest clientDTO,
                                 BindingResult bindingResult){
        if (bindingResult.hasErrors()) {
            return "client/client_top_up";
        }
        clientService.topUpClientByEmail(principal.getName(), clientDTO);
        return "redirect:/clients/account/" + principal.getName();
    }

}
