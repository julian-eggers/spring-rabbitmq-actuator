package com.itelg.spring.actuator.rabbitmq;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;

import com.itelg.spring.actuator.rabbitmq.QueueCheck;

public class QueueCheckTest
{
    @Test
    public void testConstructor()
    {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);

        Queue queue = new Queue("test");
        queue.setAdminsThatShouldDeclare(rabbitAdmin);

        QueueCheck queueCheck = new QueueCheck(queue, 10000, 2);
        Assert.assertEquals(10000, queueCheck.getMaxMessageCount());
        Assert.assertEquals(2, queueCheck.getMinConsumerCount());
        Assert.assertNotNull(queueCheck.getQueue());
        Assert.assertNotNull(queueCheck.getRabbitAdmin());
    }
}