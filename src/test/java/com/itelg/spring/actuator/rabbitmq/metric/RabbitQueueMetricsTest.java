package com.itelg.spring.actuator.rabbitmq.metric;

import static com.itelg.spring.actuator.rabbitmq.util.QueueUtil.generateQueue;
import static java.util.Collections.singletonList;

import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.api.easymock.annotation.Mock;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.springframework.amqp.core.Queue;
import org.springframework.boot.actuate.metrics.Metric;

import com.itelg.spring.actuator.rabbitmq.QueueCheck;
import com.itelg.spring.actuator.rabbitmq.RabbitQueueProperties;
import com.itelg.spring.actuator.rabbitmq.RabbitQueuePropertiesManager;

@RunWith(PowerMockRunner.class)
public class RabbitQueueMetricsTest
{
    private RabbitQueueMetrics rabbitQueueMetrics;

    @Mock
    private RabbitQueuePropertiesManager propertiesManager;

    @Before
    public void before()
    {
        rabbitQueueMetrics = new RabbitQueueMetrics();
        Whitebox.setInternalState(rabbitQueueMetrics, propertiesManager);
    }

    @Test
    public void testConstructor()
    {
        rabbitQueueMetrics = new RabbitQueueMetrics(singletonList(generateQueue("test")));
        QueueCheck queueCheck = rabbitQueueMetrics.getQueueChecks().get(0);
        Assert.assertEquals(0, queueCheck.getMaxMessageCount());
        Assert.assertEquals(0, queueCheck.getMinConsumerCount());
    }

    @Test
    public void testAddQueue()
    {
        rabbitQueueMetrics.addQueue(generateQueue("test"));
        QueueCheck queueCheck = rabbitQueueMetrics.getQueueChecks().get(0);
        Assert.assertEquals(0, queueCheck.getMaxMessageCount());
        Assert.assertEquals(0, queueCheck.getMinConsumerCount());
    }

    @Test
    public void testAddQueueWithMaxMessageCount()
    {
        rabbitQueueMetrics.addQueue(generateQueue("test"), 10000);
        QueueCheck queueCheck = rabbitQueueMetrics.getQueueChecks().get(0);
        Assert.assertEquals(10000, queueCheck.getMaxMessageCount());
        Assert.assertEquals(0, queueCheck.getMinConsumerCount());
    }

    @Test
    public void testAddQueueWithMaxMessageAndMinConsumerCount()
    {
        rabbitQueueMetrics.addQueue(generateQueue("test"), 10000, 2);
        QueueCheck queueCheck = rabbitQueueMetrics.getQueueChecks().get(0);
        Assert.assertEquals(10000, queueCheck.getMaxMessageCount());
        Assert.assertEquals(2, queueCheck.getMinConsumerCount());
    }

    @Test
    public void testMetrics()
    {
        rabbitQueueMetrics.addQueue(generateQueue("test.queue"));

        propertiesManager.request(EasyMock.anyObject(Queue.class));
        PowerMock.expectLastCall().andAnswer(() ->
        {
            RabbitQueueProperties properties = new RabbitQueueProperties();
            properties.setMessageCount(5432);
            properties.setConsumerCount(2);
            return properties;
        });

        PowerMock.replayAll();
        List<Metric<?>> metrics = new ArrayList<>(rabbitQueueMetrics.metrics());
        PowerMock.verifyAll();

        Assert.assertEquals(2, metrics.size());
        Assert.assertEquals("rabbit.queue.test_queue.currentMessageCount", metrics.get(0).getName());
        Assert.assertEquals(5432, metrics.get(0).getValue());
        Assert.assertEquals("rabbit.queue.test_queue.currentConsumerCount", metrics.get(1).getName());
        Assert.assertEquals(2, metrics.get(1).getValue());
    }

    @Test
    public void testMetricsWithThreshold()
    {
        rabbitQueueMetrics.addQueue(generateQueue("test.queue"), 10000, 5);

        propertiesManager.request(EasyMock.anyObject(Queue.class));
        PowerMock.expectLastCall().andAnswer(() ->
        {
            RabbitQueueProperties properties = new RabbitQueueProperties();
            properties.setMessageCount(5432);
            properties.setConsumerCount(2);
            return properties;
        });

        PowerMock.replayAll();
        List<Metric<?>> metrics = new ArrayList<>(rabbitQueueMetrics.metrics());
        PowerMock.verifyAll();

        Assert.assertEquals(4, metrics.size());
        Assert.assertEquals("rabbit.queue.test_queue.currentMessageCount", metrics.get(0).getName());
        Assert.assertEquals(5432, metrics.get(0).getValue());
        Assert.assertEquals("rabbit.queue.test_queue.currentConsumerCount", metrics.get(1).getName());
        Assert.assertEquals(2, metrics.get(1).getValue());
        Assert.assertEquals("rabbit.queue.test_queue.maxMessageCount", metrics.get(2).getName());
        Assert.assertEquals(10000, metrics.get(2).getValue());
        Assert.assertEquals("rabbit.queue.test_queue.minConsumerCount", metrics.get(3).getName());
        Assert.assertEquals(5, metrics.get(3).getValue());
    }

    @Test
    public void testMetricsWithException()
    {
        rabbitQueueMetrics.addQueue(generateQueue("test.queue"));

        propertiesManager.request(EasyMock.anyObject(Queue.class));
        PowerMock.expectLastCall().andThrow(new RuntimeException());

        PowerMock.replayAll();
        List<Metric<?>> metrics = new ArrayList<>(rabbitQueueMetrics.metrics());
        PowerMock.verifyAll();

        Assert.assertEquals(0, metrics.size());
    }
}