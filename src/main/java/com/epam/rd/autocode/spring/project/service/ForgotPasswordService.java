package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.model.ForgotPassword;

public interface ForgotPasswordService {
    void saveForgotPassword(ForgotPassword forgotPassword);
    void deleteById(Long id);
    ForgotPassword getForgotPassword(String email);

    ForgotPassword getForgotPassword(String email, Integer otp);

    void deleteByEmail(String email);
}
