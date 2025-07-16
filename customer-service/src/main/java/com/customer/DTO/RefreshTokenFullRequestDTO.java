package com.customer.DTO;

import com.customer.loginmodels.AuthRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenFullRequestDTO {
    private String refreshToken;
    private AuthRequest authRequest;
//    Note: using DTO class as two request body are not allowed in a singleMethod
}

