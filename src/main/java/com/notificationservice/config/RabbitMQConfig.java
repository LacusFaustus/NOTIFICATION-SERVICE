package com.notificationservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String NOTIFICATION_QUEUE = "notification.queue";
    public static final String NOTIFICATION_DLQ = "notification.dlq";
    public static final String NOTIFICATION_EXCHANGE = "notification.exchange";
    public static final String NOTIFICATION_DLQ_EXCHANGE = "notification.dlq.exchange";
    public static final String NOTIFICATION_ROUTING_KEY = "notification.routing.key";
    public static final String NOTIFICATION_DLQ_ROUTING_KEY = "notification.dlq.routing.key";

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(NOTIFICATION_QUEUE)
                .withArgument("x-dead-letter-exchange", NOTIFICATION_DLQ_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", NOTIFICATION_DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue notificationDLQ() {
        return QueueBuilder.durable(NOTIFICATION_DLQ).build();
    }

    @Bean
    public DirectExchange notificationExchange() {
        return new DirectExchange(NOTIFICATION_EXCHANGE);
    }

    @Bean
    public DirectExchange notificationDLQExchange() {
        return new DirectExchange(NOTIFICATION_DLQ_EXCHANGE);
    }

    @Bean
    public Binding notificationBinding(Queue notificationQueue, DirectExchange notificationExchange) {
        return BindingBuilder.bind(notificationQueue)
                .to(notificationExchange)
                .with(NOTIFICATION_ROUTING_KEY);
    }

    @Bean
    public Binding notificationDLQBinding(Queue notificationDLQ, DirectExchange notificationDLQExchange) {
        return BindingBuilder.bind(notificationDLQ)
                .to(notificationDLQExchange)
                .with(NOTIFICATION_DLQ_ROUTING_KEY);
    }
}
