package com.itelg.spring.actuator.rabbitmq;

import java.util.Properties;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.util.Assert;

public class RabbitQueuePropertiesManager
{
    public RabbitQueueProperties request(Queue queue)
    {
        RabbitAdmin rabbitAdmin = validateRabbitAdmin(queue);
        Properties queueProperties = rabbitAdmin.getQueueProperties(queue.getName());

        if (queueProperties == null)
        {
            throw new RuntimeException("Failed to fetch queue-properties (Queue: " + queue.getName() + ")");
        }

        RabbitQueueProperties properties = new RabbitQueueProperties();
        properties.setConsumerCount(Integer.parseInt(queueProperties.get("QUEUE_CONSUMER_COUNT").toString()));
        properties.setMessageCount(Integer.parseInt(queueProperties.get("QUEUE_MESSAGE_COUNT").toString()));
        return properties;
    }

    private RabbitAdmin validateRabbitAdmin(Queue queue)
    {
        Assert.notEmpty(queue.getDeclaringAdmins(), "At least one RabbitAdmin must be declared (Queue: " + queue.getName() + ")");
        Object object = queue.getDeclaringAdmins().iterator().next();
        Assert.isInstanceOf(RabbitAdmin.class, object, "DeclaringAdmin must be a RabbitAdmin (Queue: " + queue.getName() + ")");
        return (RabbitAdmin) object;
    }
}