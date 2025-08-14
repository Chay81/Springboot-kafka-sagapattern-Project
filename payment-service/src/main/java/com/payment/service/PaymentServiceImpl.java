package com.payment.service;

import com.payment.DTO.PaymentRequestDTO;
import com.payment.DTO.PaymentResponseDTO;
import com.payment.entity.Payment;
import com.payment.entity.PaymentStatus;
import com.payment.mapper.PaymentMapper;
import com.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentMapper paymentMapper;

    @Override
    public PaymentResponseDTO processPayment(PaymentRequestDTO requestDTO) {
        // Simulate payment success
        Payment payment = paymentMapper.toEntity(requestDTO);
        payment.setStatus(PaymentStatus.SUCCESS);
        Payment saved = paymentRepository.save(payment);

        return paymentMapper.toDTO(saved, "✅ Payment processed successfully");
    }

    @Override
    public PaymentResponseDTO getPaymentDetails(String orderId) {
        Optional<Payment> optionalPayment = paymentRepository.findByOrderId(orderId);
        if (optionalPayment.isEmpty()) {
            return PaymentResponseDTO.builder()
                    .orderId(orderId)
                    .message("❌ Payment not found for orderId: " + orderId)
                    .build();
        }
        return paymentMapper.toDTO(optionalPayment.get(), "✅ Payment details found");
    }
}

