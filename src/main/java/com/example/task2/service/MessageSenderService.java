package com.example.task2.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.task2.dto.EmailDto;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MessageSenderService {

    private final RabbitTemplate rabbitTemplate;

    @Value("${spring.rabbitmq.exchangeName}")
    private String exchangeName;

    @Value("${spring.rabbitmq.queueName}")
    private String queueName;

    public void sendEmailTask(EmailDto emailDto) {
        rabbitTemplate.convertAndSend(exchangeName, queueName, emailDto);
        
        System.out.println("Message sent to RabbitMQ: " + emailDto.getToEmail());
    }
}

