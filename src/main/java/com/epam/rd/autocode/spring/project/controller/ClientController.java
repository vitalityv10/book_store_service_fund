package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.aop.SecurityLoggingEvent;
import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.dto.ClientFilter;
import com.epam.rd.autocode.spring.project.dto.topUp.ClientTopUpRequest;
import com.epam.rd.autocode.spring.project.dto.PageResponse;
import com.epam.rd.autocode.spring.project.service.ClientService;
import jakarta.servlet.http.HttpServletResponse;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

import static com.epam.rd.autocode.spring.project.util.CookieUtils.clearCookie;

@Controller
@RequestMapping("/clients")
@RequiredArgsConstructor
public class ClientController {
  private final ClientService clientService;

    @GetMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE')")
    @SecurityLoggingEvent(message = "Clients review requested")
    public String getAllClients(ClientFilter clientFilter, @PageableDefault(size = 5) Pageable pageable, Model model) {
        Page<ClientFilter> clientPage = clientService.getClientsByFilter(clientFilter, pageable);
        model.addAttribute("clients", PageResponse.of(clientPage));
        model.addAttribute("clientFilter", clientFilter);
        return "client/clients";
    }

    @GetMapping("/block")
    @PreAuthorize("hasAnyRole('EMPLOYEE')")
    @SecurityLoggingEvent(message = "Client un/block requested")
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
        model.addAttribute("canDelete", clientService.canClientBeDeleted(email));
        return "client/client_account_info";
    }

    @GetMapping("/account/edit/{email}")
    public String showEditForm(@PathVariable("email") String email, Model model) {
        ClientDTO clientDTO = clientService.getClientByEmail(email);
        model.addAttribute("client", clientDTO);
        return "client/client_account_edit";
    }

    @PatchMapping("/account/edit/{email}")
    @SecurityLoggingEvent(message = "Client update requested")
    public String editMyAccount(@ModelAttribute("client")@Valid ClientDTO clientDTO,
                                BindingResult bindingResult,
                                @PathVariable("email") String email){
        if (bindingResult.hasErrors()) {
            return "client/client_account_edit";
        }
        try {
            clientService.updateClientByEmail(email, clientDTO);
        } catch (IllegalArgumentException ex) {
            bindingResult.rejectValue("password", "error.password.mismatch", "Passwords do not match");
            return "client/client_account_edit";
        }
        //clientService.updateClientByEmail(email, clientDTO);
        return "redirect:/clients/account/" + clientDTO.getEmail();
    }

    @DeleteMapping("/account/delete/{email}")
    @SecurityLoggingEvent(message = "Client delete submitted")
    public String deleteMyAccount(@PathVariable("email") String email,
                                  HttpServletResponse response){
        clientService.deleteClientByEmail(email);

        clearCookie(response, "JWT");
        clearCookie(response, "RefreshJWT");

        return "redirect:/books";
    }

    @GetMapping("/account/topUp")
    public String showTopUp(Principal principal, Model model) {
        ClientDTO clientDTO = clientService.getClientByEmail(principal.getName());
        model.addAttribute("client", clientDTO);
        return "client/client_top_up";
    }

    @PatchMapping("/account/topUp")
    @SecurityLoggingEvent(message = "Client top up requested")
    public String topUpMyAccount(Principal principal,
                                 @ModelAttribute("client") ClientTopUpRequest clientDTO,
                                 BindingResult bindingResult){
        if (bindingResult.hasErrors()) {
            return "client/client_top_up";
        }
        clientService.topUpClientByEmail(principal.getName(), clientDTO);
        return "redirect:/clients/account/" + principal.getName();
    }


    @PatchMapping("/account/withdraw")
    @SecurityLoggingEvent(message = "Client top up requested")
    public String withdraw(Principal principal,
                           @ModelAttribute("client") ClientTopUpRequest clientDTO){
        clientService.withdraw(principal.getName(), clientDTO);
        return "redirect:/clients/account/" + principal.getName();
    }


}
