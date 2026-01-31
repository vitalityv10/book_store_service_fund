package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.aop.BusinessLoggingEvent;
import com.epam.rd.autocode.spring.project.dto.*;
import com.epam.rd.autocode.spring.project.dto.topUp.*;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.QClient;
import com.epam.rd.autocode.spring.project.model.enums.Role;
import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import com.epam.rd.autocode.spring.project.repo.OrderRepository;
import com.epam.rd.autocode.spring.project.service.ClientService;
import com.querydsl.core.BooleanBuilder;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientServiceImpl implements ClientService {
    private final PasswordEncoder passwordEncoder;
    private final ClientRepository clientRepository;
    private final ModelMapper modelMapper;
    private final OrderRepository orderRepository;

    @Override
    public ClientDTO getClientByEmail(String email) {
        return clientRepository.findByEmail(email)
                .map(client -> modelMapper.map(client, ClientDTO.class))
                .orElseThrow(() -> new NotFoundException("Client not found"));
    }

    @Override
    @Transactional
    @BusinessLoggingEvent(message = "Updating client")
    public ClientDTO updateClientByEmail(String email, ClientDTO client) {
        Client clientToUpdate = findClientByEmail(email);

        if(passwordEncoder.matches(client.getPassword(), clientToUpdate.getPassword()))
        {
           // clientToUpdate.setPassword(passwordEncoder.encode(client.getPassword()));
            clientToUpdate.setName(client.getName());
            clientToUpdate.setEmail(client.getEmail());
            Client clientUpdated = clientRepository.save(clientToUpdate);
            return modelMapper.map(clientUpdated, ClientDTO.class);
        }else{
            throw new IllegalArgumentException("password.mismatch");
        }
    }

    @Override
    @Transactional
    @BusinessLoggingEvent(message = "Client deleting ")
    public void deleteClientByEmail(String email) {
        if (!canClientBeDeleted(email)) {
            throw new IllegalStateException("error.client.has.active.orders");
        }
        Client client = findClientByEmail(email);
        clientRepository.delete(client);
    }

    @Override
    @Transactional
    @BusinessLoggingEvent(message = "Client registration")
    public ClientDTO addClient(ClientDTO clientDto) {
        Client client = new Client();
        client.setName(clientDto.getName());
        client.setEmail(clientDto.getEmail());
        client.setPassword(passwordEncoder.encode(clientDto.getPassword()));
        client.setRoles(Collections.singleton(Role.valueOf("ROLE_CLIENT")));

        clientRepository.save(client);
        return modelMapper.map(client, ClientDTO.class);
    }

    @Override
    public Page<ClientFilter> getClientsByFilter(ClientFilter clientFilter, Pageable pageable) {
        QClient client = QClient.client;

        BooleanBuilder where = new BooleanBuilder();

        if (clientFilter.getName() != null && !clientFilter.getName().isBlank()) {
            where.and(client.name.containsIgnoreCase(clientFilter.getName()));
        }

        return clientRepository.findAll(where, pageable)
                .map(c -> {
                    ClientFilter dto = modelMapper.map(c, ClientFilter.class);
                    dto.setBlocked(c.getRoles().contains(Role.ROLE_BLOCKED));
                    return dto;
                });
    }

    @Override
    @Transactional
    @BusinessLoggingEvent( message = "Client unblocked")
    public void unblockClient(String email) {
        Client client = findClientByEmail(email);

        client.getRoles().remove(Role.valueOf("ROLE_BLOCKED"));
        clientRepository.save(client);
    }

    @Override
    @Transactional
    @BusinessLoggingEvent(message = "Client blocked")
    public ClientDTO blockClient(String email) {
        Client client = findClientByEmail(email);

        if (!client.getRoles().contains(Role.valueOf("ROLE_BLOCKED"))) {
            client.getRoles().add(Role.ROLE_BLOCKED);
            clientRepository.save(client);
        }

        return modelMapper.map(client, ClientDTO.class);
    }

    @Override
    @Transactional
    @BusinessLoggingEvent(message = "Client top up his balance")
    public ClientTopUpResponse topUpClientByEmail(String email, ClientTopUpRequest clientTopUpRequest) {
        Client clientToTopUp = findClientByEmail(email);
        BigDecimal clientBalance = clientToTopUp.getBalance();

        if (clientTopUpRequest.balance() == null || clientTopUpRequest.balance().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Top-up amount must be greater than zero");
        }

        BigDecimal totalBalance = clientBalance.add(clientTopUpRequest.balance());
        clientToTopUp.setBalance(totalBalance);
        Client topUpClient =  clientRepository.save(clientToTopUp);
        return modelMapper.map(topUpClient, ClientTopUpResponse.class);
    }

    @Override
    @Transactional
    @BusinessLoggingEvent(message = "Client withdrawing")
    public ClientTopUpResponse withdraw(String email, ClientTopUpRequest clientTopUpRequest) {
        Client clientToWithdraw = findClientByEmail(email);
        BigDecimal clientBalance = clientToWithdraw.getBalance();
        BigDecimal withdrawAmount = clientTopUpRequest.balance();

        if (withdrawAmount == null || withdrawAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdraw amount must be greater than zero");
        }

        if (clientBalance.compareTo(withdrawAmount) < 0) {
            throw new IllegalArgumentException("Insufficient balance");
        }

        BigDecimal totalBalance = clientBalance.subtract(withdrawAmount);
        clientToWithdraw.setBalance(totalBalance);
        Client topUpClient =  clientRepository.save(clientToWithdraw);
        return modelMapper.map(topUpClient, ClientTopUpResponse.class);
    }


    private Client findClientByEmail(String email) {
        return clientRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Client not found"));
    }

    public boolean canClientBeDeleted(String email) {
        Client client = findClientByEmail(email);
        List<String> allowedStatuses = List.of("REFUNDED", "DELIVERED");
        boolean isNotZero = client.getBalance().compareTo(BigDecimal.ZERO) != 0;
        return !orderRepository.existsByClientIdAndOrderStatusNotIn(client.getId(), allowedStatuses)
                && !isNotZero ;
    }
}
