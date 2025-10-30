package com.innowise.internship.service.impl;

import com.innowise.internship.dto.kafka.PaymentCreatedEvent;
import com.innowise.internship.service.KafkaConsumerService;
import com.innowise.internship.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaConsumerServiceImpl implements KafkaConsumerService {

    private final OrderService orderService;

    @Override
    @KafkaListener(
            topics = "${app.kafka.topic.create-payment}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void handlePaymentCreatedEvent(PaymentCreatedEvent paymentCreatedEvent) {

        Long orderId = Long.parseLong(paymentCreatedEvent.getOrderId());
        String paymentStatus = paymentCreatedEvent.getPaymentStatus();
        orderService.updateOrderStatusAfterPayment(orderId, paymentStatus);

    }
}
