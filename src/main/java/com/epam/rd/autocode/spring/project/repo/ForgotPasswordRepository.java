package com.epam.rd.autocode.spring.project.repo;

import com.epam.rd.autocode.spring.project.model.ForgotPassword;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ForgotPasswordRepository extends CrudRepository<ForgotPassword, String> {
  Optional< ForgotPassword> getByEmailAndOtp(String email, Integer otp);

    void deleteById(Long id);

    void deleteByEmail(String email);
    boolean existsByEmail(String email);
}
