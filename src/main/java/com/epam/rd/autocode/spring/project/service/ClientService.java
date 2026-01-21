package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.*;
import com.epam.rd.autocode.spring.project.dto.topUp.ClientTopUpRequest;
import com.epam.rd.autocode.spring.project.dto.topUp.ClientTopUpResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ClientService {

    List<ClientDTO> getAllClients();

    ClientDTO getClientByEmail(String email);

    ClientDTO updateClientByEmail(String email, ClientDTO client);

    void deleteClientByEmail(String email);

    ClientDTO addClient(ClientDTO client);

    Page<ClientFilter> getClientsByFilter(ClientFilter clientFilter , Pageable pageable);

    void unblockClient(String email);

    ClientDTO blockClient(String email);

    ClientTopUpResponse topUpClientByEmail(String email, ClientTopUpRequest clientTopUpRequest);
}
