package com.customer.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ForgotPasswordRequestDTO {
    private String email;
    private String phoneNumber;
    private String newPassword;
    private String retypePassword;
}
