package com.payment.DTO;

import com.payment.entity.PaymentStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentStatusUpdateDTO {
    private Long paymentId;
    private PaymentStatus status;
}

