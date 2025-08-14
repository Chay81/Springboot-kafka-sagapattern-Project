package com.payment.mapper;

import com.payment.DTO.PaymentRequestDTO;
import com.payment.DTO.PaymentResponseDTO;
import com.payment.entity.Payment;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public Payment toEntity(PaymentRequestDTO dto) {
        return Payment.builder()
                .orderId(dto.getOrderId())
                .customerEmail(dto.getCustomerEmail())
                .amount(dto.getAmount())
                .build();
    }

    public PaymentResponseDTO toDTO(Payment payment, String message) {
        return PaymentResponseDTO.builder()
                .paymentId(payment.getId())
                .orderId(payment.getOrderId())
                .customerEmail(payment.getCustomerEmail())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .message(message)
                .build();
    }
}

