package com.payment.DTO;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequestDTO {
    private String orderId;
    private String customerEmail;
    private Double amount;
}

