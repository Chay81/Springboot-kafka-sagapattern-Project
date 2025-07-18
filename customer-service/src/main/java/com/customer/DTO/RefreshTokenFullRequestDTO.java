package com.customer.DTO;

import com.customer.loginmodels.AuthRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenFullRequestDTO {

    @NotBlank(message = "Refresh token must not be blank")
    private String refreshToken;

    @Valid
    private AuthRequest authRequest;
//    Note: using DTO class as two request body are not allowed in a singleMethod
}

