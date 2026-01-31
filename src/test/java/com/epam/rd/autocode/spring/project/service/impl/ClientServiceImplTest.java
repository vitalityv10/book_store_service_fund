package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.dto.ClientFilter;
import com.epam.rd.autocode.spring.project.dto.topUp.ClientTopUpRequest;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.model.enums.Role;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import com.epam.rd.autocode.spring.project.service.ClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ClientServiceImplTest {
    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ClientService clientService;

    private Client client;

    @BeforeEach
    void setUp() {
        client = new Client();
        client.setName("Walter");
        client.setEmail("walterw@example.com");
        client.setPassword("$2a$10$8kbSX.9C602LS9nIqYf6IOkF1u9Vw4PfWQ4.rOTby9LlsfL6OiGke");
        client.setRoles(new HashSet<>(Set.of(Role.ROLE_CLIENT)));
        clientRepository.save(client);
    }

    @Test
    void getClientByEmail_Success() {
        ClientDTO foundClient = clientService.getClientByEmail(client.getEmail());

        assertNotNull(foundClient);
        assertEquals(client.getName(), foundClient.getName());
        assertEquals(client.getEmail(), foundClient.getEmail());
    }
    @Test
    void getClientByEmail_NotExistsEmail_ReturnsNotFound() {
        String email = "sa@gmail.com";
        NotFoundException exception = assertThrows(NotFoundException.class,
                        () -> clientService.getClientByEmail(email));

        assertEquals("Client not found", exception.getMessage());
    }

    @Test
    void updateClientByEmail_Success() {
        String oldEmail = client.getEmail();
        ClientDTO updateDto = ClientDTO.builder()
                .name("Updated Name")
                .email("new.email@example.com")
                .password("securepass")
                .build();

        ClientDTO result = clientService.updateClientByEmail(oldEmail, updateDto);

        assertEquals("Updated Name", result.getName());
        assertEquals("new.email@example.com", result.getEmail());

        assertThrows(NotFoundException.class, () -> clientService.getClientByEmail(oldEmail));
    }

    @Test
    void updateClientByEmail_EmailNotFound() {
        ClientDTO clientUpdateInfo = ClientDTO.builder()
                .name("Updated Name")
                .email("new.email@example.com")
                .build();

        ClientDTO clientToUpdate = clientService.getClientByEmail(client.getEmail());
        clientToUpdate.setName(clientUpdateInfo.getName());
        clientToUpdate.setEmail(clientUpdateInfo.getEmail());

        assertThrows(NotFoundException.class, () ->clientService
                .updateClientByEmail(clientToUpdate.getEmail(), clientToUpdate));
    }

    @Test
    void deleteClientByEmail_Success() {
        clientService.deleteClientByEmail(client.getEmail());

        assertThrows(NotFoundException.class, () ->
                clientService.getClientByEmail(client.getEmail()));
    }

    @Test
    void deleteClientByEmail_EmailNotFund() {
      String email = "walterw@g.com";
        assertThrows(NotFoundException.class, () ->
                clientService.deleteClientByEmail(email));
    }

    @Test
    void addClient_SuccessReturnClient() {
        ClientDTO newClientDto = ClientDTO.builder()
                .email("Jesse.Pinkman@example.com")
                .name("Jesse")
                .password("science")
                .build();

        ClientDTO savedClient = clientService.addClient(newClientDto);

        assertNotNull(savedClient);
        assertEquals(newClientDto.getEmail(), savedClient.getEmail());

        assertTrue(clientRepository.findByEmail(newClientDto.getEmail()).isPresent());
    }


    @Test
    void getClientsByFilter_Success() {
        Pageable pageable = PageRequest.of(0, 5);

        Page<ClientFilter> result = clientService.getClientsByFilter(new ClientFilter(), pageable);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(11, result.getTotalElements());
    }

    @Test
    void getClientsByFilter_NameSuccess() {
        ClientFilter filter = new ClientFilter();
        filter.setName("Walter");
        Pageable pageable = PageRequest.of(0, 5);

        Page<ClientFilter> result = clientService.getClientsByFilter(filter, pageable);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.getContent().stream()
                .anyMatch(c -> c.getName().contains("Walter")));
    }

    @Test
    void blockClient_Success() {
        clientService.blockClient(client.getEmail());

        Client updatedClient = clientRepository.findByEmail(client.getEmail()).get();

        assertTrue(updatedClient.getRoles().contains(Role.ROLE_BLOCKED));
    }

    @Test
    void unblockClient_Success() {
        client.getRoles().add(Role.ROLE_BLOCKED);
        clientRepository.save(client);

        clientService.unblockClient(client.getEmail());

        Client updatedClient = clientRepository.findByEmail(client.getEmail()).get();
        assertFalse(updatedClient.getRoles().contains(Role.ROLE_BLOCKED));
    }

    @Test
    void topUpClientByEmail_Success() {
        ClientTopUpRequest clientTopUpRequest = new ClientTopUpRequest(client.getEmail(),new BigDecimal("100") );
        BigDecimal initialBalance = client.getBalance() != null ? client.getBalance() : BigDecimal.ZERO;

        clientService.topUpClientByEmail(client.getEmail(), clientTopUpRequest);

        Client updatedClient = clientRepository.findByEmail(client.getEmail()).get();
        BigDecimal expectedBalance = initialBalance.add(clientTopUpRequest.balance());

        assertEquals(0, expectedBalance.compareTo(updatedClient.getBalance()));
    }

    @Test
    void topUpClientByEmail_NegativeAmount_ThrowsException() {
        ClientTopUpRequest clientTopUpRequest = new ClientTopUpRequest(client.getEmail(),new BigDecimal("-100.00") );
         assertThrows(IllegalArgumentException.class, () ->
         clientService.topUpClientByEmail(client.getEmail(),  clientTopUpRequest));
    }
}