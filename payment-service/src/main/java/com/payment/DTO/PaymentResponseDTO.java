package com.payment.DTO;

import com.payment.entity.PaymentStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponseDTO {
    private Long paymentId;
    private String orderId;
    private String customerEmail;
    private Double amount;
    private PaymentStatus status;
    private String message;
}

