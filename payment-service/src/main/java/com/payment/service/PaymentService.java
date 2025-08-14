package com.payment.service;

import com.payment.DTO.PaymentRequestDTO;
import com.payment.DTO.PaymentResponseDTO;

public interface PaymentService {
    PaymentResponseDTO processPayment(PaymentRequestDTO requestDTO);
    PaymentResponseDTO getPaymentDetails(String orderId);
}

