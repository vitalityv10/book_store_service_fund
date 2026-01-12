package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.UserRegisterResponseDto;
import com.epam.rd.autocode.spring.project.entity.UserEntity;
import com.epam.rd.autocode.spring.project.model.enums.Role;
import com.epam.rd.autocode.spring.project.repo.UserRepository;
import com.epam.rd.autocode.spring.project.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImp implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    public Optional<UserRegisterResponseDto> findByEmail(String email) {
        log.info("findByEmail(email={})", email);
        return userRepository.findByEmail(email)
                .map(userEntity -> modelMapper.map(userEntity, UserRegisterResponseDto.class));
    }


    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserRegisterResponseDto userRegisterRequestDto = findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));

        return new User(
                userRegisterRequestDto.getEmail(),
                userRegisterRequestDto.getPassword(),
                userRegisterRequestDto.getRoles().stream().map(role -> new SimpleGrantedAuthority("ROLE_"+role.name())).collect(Collectors.toSet())
        );
    }

    public void createUser(UserRegisterResponseDto userRegisterRequestDto) {
        userRegisterRequestDto.setRoles(Set.of(Role.CLIENT));
        UserEntity userEntity = modelMapper.map(userRegisterRequestDto, UserEntity.class);
        userEntity.setPassword(passwordEncoder.encode(userRegisterRequestDto.getPassword()));
        userRepository.save(userEntity);
    }

}
