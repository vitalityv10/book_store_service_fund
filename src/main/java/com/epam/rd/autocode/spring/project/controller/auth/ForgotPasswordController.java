package com.epam.rd.autocode.spring.project.controller.auth;

import com.epam.rd.autocode.spring.project.aop.SecurityLoggingEvent;
import com.epam.rd.autocode.spring.project.dto.ChangePassword;
import com.epam.rd.autocode.spring.project.model.ForgotPassword;
import com.epam.rd.autocode.spring.project.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Controller
@RequestMapping("/auth/forgot-password")
@RequiredArgsConstructor
public class ForgotPasswordController {
    private final UserService userService;
    private final EmailService emailService;
    private final ForgotPasswordService forgotPasswordService;

    @GetMapping
    public String showForm() {
        return "auth/forgot_password";
    }

    @PostMapping("/verify-mail")
    @SecurityLoggingEvent(message = "Forgot password requested")
    public String verifyMail(@RequestParam("email") String email, Model model) {
        if (!userService.existsByEmail(email)){
            model.addAttribute("error", "Email doesn't exist");
            return "redirect:/auth/forgot_password";
        };
        ForgotPassword forgotPassword = forgotPasswordService.getForgotPassword(email);
        emailService.sendMessage(email, forgotPassword.getOtp());
        forgotPasswordService.saveForgotPassword(forgotPassword);

        model.addAttribute("email", email);
        return "auth/verify_otp";
    }

    @GetMapping("/verify-otp-page/{email}")
    public String showOtpPage(@PathVariable("email") String email, Model model) {
        model.addAttribute("email", email);
        return "auth/verify_otp";
    }

    @PostMapping("/verify-otp")
    @SecurityLoggingEvent(message = "OTP verified submitted")
    public String verifyOTP(@RequestParam("email") String email,
                            @RequestParam("otp") Integer otp,
                            Model model) {
        ForgotPassword fp = forgotPasswordService.getForgotPassword(email, otp);

        if(fp.getExpiryDate().isBefore(LocalDateTime.now())){
            forgotPasswordService.deleteById(fp.getId());

            ForgotPassword newFp = forgotPasswordService.getForgotPassword(email);
            emailService.sendMessage(email, newFp.getOtp());
            forgotPasswordService.saveForgotPassword(newFp);

            model.addAttribute("email", email);
            model.addAttribute("error", "OTP has expired");
            return "auth/verify_otp";
        }

        model.addAttribute("email", email);
        model.addAttribute("changePassword", new ChangePassword());
        return "auth/new_password";
    }


    @PatchMapping("/change-password/{email}")
    @SecurityLoggingEvent(message = "Password changing requested")
    public String changePassword(@PathVariable("email") String email,
                                 @ModelAttribute("changePassword") @Valid ChangePassword changePassword,
                                 BindingResult result,
                                 Model model) {

        if (result.hasErrors()) {
            model.addAttribute("email", email);
            return "auth/new_password";
        }

        if (!Objects.equals(changePassword.getPassword(), changePassword.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "error.changePassword", "Passwords do not match");
            model.addAttribute("email", email);
            return "auth/new_password";
        }

        userService.updatePassword(email, changePassword.getPassword());
        forgotPasswordService.deleteByEmail(email);

        return "redirect:/auth/login";
    }

}
