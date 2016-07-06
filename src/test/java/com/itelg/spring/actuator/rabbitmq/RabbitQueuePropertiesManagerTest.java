package com.itelg.spring.actuator.rabbitmq;

import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.api.easymock.annotation.Mock;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;

import com.itelg.spring.actuator.rabbitmq.RabbitQueueProperties;
import com.itelg.spring.actuator.rabbitmq.RabbitQueuePropertiesManager;

@RunWith(PowerMockRunner.class)
public class RabbitQueuePropertiesManagerTest
{
    private RabbitQueuePropertiesManager propertiesManager = new RabbitQueuePropertiesManager();

    @Mock
    private RabbitAdmin rabbitAdmin;

    @Test
    public void testRequest()
    {
        Queue queue = new Queue("test");
        queue.setAdminsThatShouldDeclare(rabbitAdmin);

        Properties properties = new Properties();
        properties.setProperty("QUEUE_CONSUMER_COUNT", "2");
        properties.setProperty("QUEUE_MESSAGE_COUNT", "234");
        rabbitAdmin.getQueueProperties("test");
        PowerMock.expectLastCall().andReturn(properties);

        PowerMock.replayAll();
        RabbitQueueProperties queueProperties = propertiesManager.request(queue);
        PowerMock.verifyAll();

        Assert.assertEquals(2, queueProperties.getConsumerCount());
        Assert.assertEquals(234, queueProperties.getMessageCount());
    }
}