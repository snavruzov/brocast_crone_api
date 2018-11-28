package com.dgtz.api.crone;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * BroCast.
 * Copyright: Sardor Navruzov
 * 2013-2016.
 */

@EnableRabbit
@Configuration
public class RabbitConfiguration {
    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory("rabbit.sys.brocast.com");
        connectionFactory.setPassword("admin");
        connectionFactory.setUsername("admin");
        return connectionFactory;
    }

    @Bean
    public AmqpAdmin amqpAdmin() {
        return new RabbitAdmin(connectionFactory());
    }

    @Bean
    public RabbitTemplate rabbitTemplate() {
        return new RabbitTemplate(connectionFactory());
    }

    @Bean
    public Queue liveQ() {
        return new Queue("liveQ");
    }

    @Bean
    public Queue liveDQ() {
        return new Queue("liveDQ");
    }

    @Bean
    public Queue liveThumbQ() {
        return new Queue("liveThumbQ");
    }
}
