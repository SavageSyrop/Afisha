package ru.it.lab.config;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MQConfig {
    public static final String QUEUE_ROLE = "role_queue";
    public static final String EXCHANGE_ROLE = "role_exchange";
    public static final String KEY_ROLE = "role_routing_key";

    public static final String QUEUE_EVENT = "event_queue";
    public static final String EXCHANGE_EVENT = "event_exchange";
    public static final String KEY_EVENT = "event_routing_key";

    @Bean
    public Queue queue1() {
        return new Queue(QUEUE_ROLE);
    }

    @Bean
    public Queue queue2() {
        return new Queue(QUEUE_EVENT);
    }

    @Bean
    public TopicExchange exchange1() {
        return new TopicExchange(EXCHANGE_ROLE);
    }

    @Bean
    public TopicExchange exchange2() {
        return new TopicExchange(EXCHANGE_EVENT);
    }


    @Bean
    public Binding binding1(@Qualifier("queue1") Queue queue, @Qualifier("exchange1") TopicExchange topicExchange) {
        return BindingBuilder
                .bind(queue)
                .to(topicExchange)
                .with(KEY_ROLE);
    }

    @Bean
    public Binding binding2(@Qualifier("queue2") Queue queue, @Qualifier("exchange2") TopicExchange topicExchange) {
        return BindingBuilder
                .bind(queue)
                .to(topicExchange)
                .with(KEY_EVENT);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public AmqpTemplate template(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
