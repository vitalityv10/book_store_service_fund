package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.*;
import com.epam.rd.autocode.spring.project.dto.topUp.ClientTopUpRequest;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.service.ClientService;
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

import java.math.BigDecimal;
import java.util.*;

import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
class ClientControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClientService clientService;

    private ClientFilter clientFilter;
    private Page<ClientFilter> clientPage;
    private ClientDTO client;

    @BeforeEach
    void setUp() {
        clientFilter = new ClientFilter();
              clientFilter.setName("Client1");
              clientFilter.setEmail("client@gmail.com");
              clientFilter.setBalance(new BigDecimal("100.00"));
              clientFilter.setBlocked(false);
        List<ClientFilter> clients = Collections.singletonList(clientFilter);
        clientPage = new PageImpl<>(clients, PageRequest.of(0, 6), 1);

        client = ClientDTO.builder()
                .password("password123")
                .email("client2@example.com")
                .name("Client 2")
                .build();
    }


    @Test
    @WithMockUser(roles = "CLIENT")
    void getAllClients_404() throws Exception {
        mockMvc.perform(get("/clients"))
                .andExpect(status().isNotFound());
    }
    @Test
    void getAllClients_Anonymous_Redirect() throws Exception {
        mockMvc.perform(get("/clients"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void getAllClients_ViewWithClients() throws Exception {
        when(clientService.getClientsByFilter(any(), any()))
                .thenReturn(clientPage);

        mockMvc.perform(get("/clients"))
                .andExpect(status().isOk())
                .andExpect(view().name("client/clients"))
                .andExpect(model().attributeExists("clients"))
                .andExpect(model().attributeExists("clientFilter"));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void getAllClients_ViewWithClientsByFilter() throws Exception {
        when(clientService.getClientsByFilter(any(ClientFilter.class), any(Pageable.class)))
                .thenReturn(clientPage);

        mockMvc.perform(get("/clients")
                        .param("name", clientFilter.getName()))
                .andExpect(status().isOk())
                .andExpect(view().name("client/clients"))
                .andExpect(model().attribute("clients", hasProperty("content", is(clientPage.getContent()))))
                .andExpect(model().attribute("clientFilter", hasProperty("name", is(clientFilter.getName()))));

        verify(clientService).getClientsByFilter(any(ClientFilter.class), any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void getAllClients_ViewWithClientsInDesc() throws Exception {
        when(clientService.getClientsByFilter(any(ClientFilter.class), any(Pageable.class)))
                .thenReturn(clientPage);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);

        mockMvc.perform(get("/clients")
                        .param("name", clientFilter.getName())
                        .param("sort", "name,desc"))
                .andDo(print())
                .andExpect(status().isOk());

        verify(clientService).getClientsByFilter(any(ClientFilter.class), captor.capture());

        Pageable pageable = captor.getValue();
        assertNotNull(pageable.getSort().getOrderFor("name"));
        assertEquals(Sort.Direction.DESC, pageable.getSort().getOrderFor("name").getDirection());
    }

    @Test
    @WithMockUser(roles = "ClIENT")
    void blockUnblockClient_404() throws Exception {
        mockMvc.perform(get("/clients/block")
                        .param("block", "true")
                        .param("email", client.getEmail()))
                .andExpect(status().isNotFound());

        verify(clientService, never()).blockClient(anyString());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void blockUnblockClient_SuccessBlock() throws Exception {
       when(clientService.blockClient(anyString()))
               .thenReturn(client);

        mockMvc.perform(get("/clients/block")
                        .param("block", "true")
                        .param("email", client.getEmail()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/clients"));

        verify(clientService, times(1)).blockClient(anyString());

    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void blockUnblockClient_SuccessUnblock() throws Exception {
       doNothing().when(clientService).unblockClient(anyString());

        mockMvc.perform(get("/clients/block")
                        .param("block", "false")
                        .param("email", client.getEmail()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/clients"));

        verify(clientService, times(1)).unblockClient(anyString());

    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void showMyAccount_Success() throws Exception {
        when(clientService.getClientByEmail(anyString()))
                .thenReturn(client);

        mockMvc.perform(get("/clients/account/{email}", client.getEmail()))
                .andExpect(status().isOk())
                .andExpect(view().name("client/client_account_info"))
                .andExpect(model().attributeExists("client"));

        verify(clientService, times(1)).getClientByEmail(anyString());
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void showMyAccount_EmailNotExist() throws Exception {
        String nonExistEmail ="sa@gmail.com";

        when(clientService.getClientByEmail(nonExistEmail))
                .thenThrow(new NotFoundException("Client not found"));

        mockMvc.perform(get("/clients/account/{email}", nonExistEmail))
                .andExpect(status().isNotFound());

        verify(clientService, times(1)).getClientByEmail(anyString());
    }

    @Test
    void showMyAccount_Anonymous_404() throws Exception {
        when(clientService.getClientByEmail(anyString()))
                .thenReturn(client);

        mockMvc.perform(get("/clients/account/{email}", client.getEmail()))
                .andExpect(status().is3xxRedirection());

        verify(clientService, never()).getClientByEmail(anyString());
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void showEditForm_Success() throws Exception {
        when(clientService.getClientByEmail(anyString()))
                .thenReturn(client);

        mockMvc.perform(get("/clients/account/edit/{email}", client.getEmail()))
                .andExpect(status().isOk())
                .andExpect(view().name("client/client_account_edit"))
                .andExpect(model().attributeExists("client"));

        verify(clientService, times(1)).getClientByEmail(anyString());
    }

    @Test
    void showEditForm_Anonymous_404() throws Exception {
        when(clientService.getClientByEmail(anyString()))
                .thenReturn(client);

        mockMvc.perform(get("/clients/edit/account/{email}", client.getEmail()))
                .andExpect(status().is3xxRedirection());

        verify(clientService, never()).getClientByEmail(anyString());
    }

    @Test
    @WithMockUser(roles="CLIENT")
    void editMyAccount_Success() throws Exception {
        String email = "client2@example.com";
        when(clientService.updateClientByEmail(eq(email), any(ClientDTO.class)))
                .thenReturn(client);

        mockMvc.perform(patch("/clients/account/edit/{email}", email)
                        .param("name", "Client 2")
                        .param("email", email)
                        .param("password", "password123"))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/clients/account/" + email));

        verify(clientService, times(1)).updateClientByEmail(eq(email), any(ClientDTO.class));
    }
    @Test
    @WithMockUser(roles="CLIENT")
    void editMyAccount_InvalidAttributes() throws Exception {
        String invalidPassword = "12";

        mockMvc.perform(patch("/clients/account/edit/{email}", client.getEmail())
                        .param("email", client.getEmail())
                        .param("password", invalidPassword))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("client/client_account_edit"))
                .andExpect(model().attributeExists("client"))
                .andExpect(model().attributeHasFieldErrors("client", "password"));

        verify(clientService, never()).updateClientByEmail(anyString(), any(ClientDTO.class));
    }


    @Test
    @WithMockUser(roles = "CLIENT", username = "sa@gmail.com")
    void deleteMyAccount() throws Exception {
        mockMvc.perform(delete("/clients/account/delete/{email}", "sa@gmail.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"));

        verify(clientService, times(1)).deleteClientByEmail("sa@gmail.com");
    }

    @Test
    @WithMockUser(roles = "CLIENT", username = "client2@example.com")
    void showTopUp() throws Exception {
        when(clientService.getClientByEmail(client.getEmail())).thenReturn(client);

        mockMvc.perform(get("/clients/account/topUp"))
                .andExpect(status().isOk())
                .andExpect(view().name("client/client_top_up"))
                .andExpect(model().attributeExists("client"))
                .andExpect(model().attribute("client", hasProperty("email", is(client.getEmail()))));
    }
    @Test
    @WithMockUser(roles = "CLIENT", username = "client2@example.com")
    void topUpMyAccount_Success() throws Exception {
        ClientTopUpRequest clientTopUpRequest = new ClientTopUpRequest(client.getEmail(), BigDecimal.valueOf(100));

        mockMvc.perform(patch("/clients/account/topUp")
                        .param("balance", clientTopUpRequest.balance().toString())
                        .param("email", clientTopUpRequest.email()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/clients/account/"+clientTopUpRequest.email()));

        verify(clientService, times(1)).topUpClientByEmail(eq(clientTopUpRequest.email()), any());
    }

}