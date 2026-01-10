package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import com.epam.rd.autocode.spring.project.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;

    @Override
    public List<ClientDTO> getAllClients() {
        return clientRepository.findAll()
                .stream()
                .map(ClientDTO::toClientDTO)
                .toList();
    }

    @Override
    public ClientDTO getClientByEmail(String email) {
        return clientRepository.findByEmail(email)
                .map(ClientDTO::toClientDTO)
                .orElse(null);
    }

    @Override
    public ClientDTO updateClientByEmail(String email, ClientDTO client) {
        return null;
    }

    @Override
    public void deleteClientByEmail(String email) {

    }

    @Override
    public ClientDTO addClient(ClientDTO client) {
        return null;
    }
    //TODO Place your code here
}
