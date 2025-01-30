package com.hmdp.config;

import com.hmdp.utils.MQConstants;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MQConfig {

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public Queue seckillQueue() {
        return QueueBuilder.durable(MQConstants.SECKILL_QUEUE).build();
    }

    @Bean
    public Exchange seckillExchange() {
        return ExchangeBuilder.directExchange(MQConstants.SECKILL_EXCHANGE).durable(true).build();
    }

    @Bean
    public Binding bindingSeckill(Queue seckillQueue, Exchange seckillExchange) {
        return BindingBuilder.bind(seckillQueue).to(seckillExchange).with(MQConstants.SECKILL_ROUTING_KEY).noargs();
    }

}

