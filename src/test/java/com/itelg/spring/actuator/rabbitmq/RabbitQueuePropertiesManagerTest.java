package com.itelg.spring.actuator.rabbitmq;

import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.api.easymock.annotation.MockStrict;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;

@RunWith(PowerMockRunner.class)
public class RabbitQueuePropertiesManagerTest
{
    private RabbitQueuePropertiesManager propertiesManager = new RabbitQueuePropertiesManager();

    @MockStrict
    private RabbitAdmin rabbitAdmin;

    @Test
    public void testRequest()
    {
        Queue queue = new Queue("test");
        queue.setAdminsThatShouldDeclare(rabbitAdmin);

        rabbitAdmin.getQueueProperties("test");
        PowerMock.expectLastCall().andAnswer(() ->
        {
            Properties properties = new Properties();
            properties.setProperty("QUEUE_CONSUMER_COUNT", "2");
            properties.setProperty("QUEUE_MESSAGE_COUNT", "234");
            return properties;
        });

        PowerMock.replayAll();
        RabbitQueueProperties queueProperties = propertiesManager.request(queue);
        PowerMock.verifyAll();

        Assert.assertEquals(2, queueProperties.getConsumerCount());
        Assert.assertEquals(234, queueProperties.getMessageCount());
    }

    @Test(expected = RuntimeException.class)
    public void testRequestWithFailed()
    {
        Queue queue = new Queue("test");
        queue.setAdminsThatShouldDeclare(rabbitAdmin);

        rabbitAdmin.getQueueProperties("test");
        PowerMock.expectLastCall().andReturn(null);

        PowerMock.replayAll();
        propertiesManager.request(queue);
        PowerMock.verifyAll();
    }
}