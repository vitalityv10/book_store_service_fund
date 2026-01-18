package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.aop.BusinessLoggingEvent;
import com.epam.rd.autocode.spring.project.dto.BookFilter;
import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.dto.ClientFilter;
import com.epam.rd.autocode.spring.project.dto.QPredicates;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.QClient;
import com.epam.rd.autocode.spring.project.model.enums.Role;
import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import com.epam.rd.autocode.spring.project.service.ClientService;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientServiceImpl implements ClientService {
    private final PasswordEncoder passwordEncoder;
    private final ClientRepository clientRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<ClientDTO> getAllClients() {
        return clientRepository.findAll()
                .stream()
                .map(client -> modelMapper.map(client, ClientDTO.class))
                .toList();
    }

    @Override
    public ClientDTO getClientByEmail(String email) {
        return clientRepository.findByEmail(email)
                .map(client -> modelMapper.map(client, ClientDTO.class))
                .orElseThrow(() -> new NotFoundException("Client not found"));
    }

    @Override
    @Transactional
    @BusinessLoggingEvent(message = "Client was updated")
    public ClientDTO updateClientByEmail(String email, ClientDTO client) {
        Client clientToUpdate =  clientRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Client not found"));
        clientToUpdate.setPassword(passwordEncoder.encode(client.getPassword()));
        clientToUpdate.setName(client.getName());
        clientToUpdate.setEmail(client.getEmail());

        //modelMapper.map(client, clientToUpdate);

        Client clientUpdated = clientRepository.save(clientToUpdate);
        return modelMapper.map(clientUpdated, ClientDTO.class);
    }

    @Override
    @Transactional
    @BusinessLoggingEvent(message = "Client was deleted")
    public void deleteClientByEmail(String email) {
        Client client =  clientRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Client not found"));
        clientRepository.delete(client);
    }

    @Override
    @Transactional
    @BusinessLoggingEvent(message = "Client was added")
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
        Client client =  clientRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Client not found"));

        client.getRoles().remove(Role.valueOf("ROLE_BLOCKED"));
        clientRepository.save(client);
    }

    @Override
    @Transactional
    @BusinessLoggingEvent( message = "Client blocked")
    public ClientDTO blockClient(String email) {
        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Client not found"));

        if (!client.getRoles().contains(Role.valueOf("ROLE_BLOCKED"))) {
            client.getRoles().add(Role.ROLE_BLOCKED);
            clientRepository.save(client);
        }

        return modelMapper.map(client, ClientDTO.class);
    }

}
