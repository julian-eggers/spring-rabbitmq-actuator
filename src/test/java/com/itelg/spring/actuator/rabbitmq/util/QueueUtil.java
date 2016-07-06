package com.itelg.spring.actuator.rabbitmq.util;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;

public class QueueUtil
{
    public static Queue generateQueue(String name)
    {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);

        Queue queue = new Queue(name);
        queue.setAdminsThatShouldDeclare(rabbitAdmin);

        return queue;
    }
}