package com.itelg.spring.actuator.rabbitmq.metric;

import static com.itelg.spring.actuator.rabbitmq.util.QueueUtil.generateQueue;
import static java.util.Collections.singletonList;

import com.itelg.spring.actuator.rabbitmq.RabbitQueueProperties;
import com.itelg.spring.actuator.rabbitmq.RabbitQueuePropertiesManager;
import io.micrometer.core.instrument.MeterRegistry;

import java.util.function.ToDoubleFunction;
import org.easymock.Capture;
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

import com.itelg.spring.actuator.rabbitmq.QueueCheck;

@RunWith(PowerMockRunner.class)
public class RabbitQueueMetricsTest
{
    private RabbitQueueMetrics rabbitQueueMetrics;

    @Mock
    private RabbitQueuePropertiesManager propertiesManager;

    @Mock
    private MeterRegistry meterRegistry;

    @Before
    public void before()
    {
        rabbitQueueMetrics = new RabbitQueueMetrics();
        Whitebox.setInternalState(rabbitQueueMetrics, propertiesManager);
    }

    @Test
    public void testConstructor()
    {
        rabbitQueueMetrics = new RabbitQueueMetrics(singletonList(generateQueue("test.queue")));
        QueueCheck queueCheck = rabbitQueueMetrics.getQueueChecks().get(0);
        Assert.assertEquals(0, queueCheck.getMaxMessageCount());
        Assert.assertEquals(0, queueCheck.getMinConsumerCount());
    }

    @Test
    public void testAddQueue()
    {
        rabbitQueueMetrics.addQueue(generateQueue("test.queue"));
        QueueCheck queueCheck = rabbitQueueMetrics.getQueueChecks().get(0);
        Assert.assertEquals(0, queueCheck.getMaxMessageCount());
        Assert.assertEquals(0, queueCheck.getMinConsumerCount());
    }

    @Test
    public void testAddQueueWithMaxMessageCount()
    {
        rabbitQueueMetrics.addQueue(generateQueue("test.queue"), 10000);
        QueueCheck queueCheck = rabbitQueueMetrics.getQueueChecks().get(0);
        Assert.assertEquals(10000, queueCheck.getMaxMessageCount());
        Assert.assertEquals(0, queueCheck.getMinConsumerCount());
    }

    @Test
    public void testAddQueueWithMaxMessageAndMinConsumerCount()
    {
        rabbitQueueMetrics.addQueue(generateQueue("test.queue"), 10000, 2);
        QueueCheck queueCheck = rabbitQueueMetrics.getQueueChecks().get(0);
        Assert.assertEquals(10000, queueCheck.getMaxMessageCount());
        Assert.assertEquals(2, queueCheck.getMinConsumerCount());
    }

    @Test
    public void testMetrics()
    {
        Queue queue = generateQueue("test.queue");
        rabbitQueueMetrics.addQueue(queue, 0, 0);

        EasyMock.expect(propertiesManager.request(queue)).andAnswer(this::buildQueueProperties).times(2);

        Capture<ToDoubleFunction<Queue>> currentMessageCountFunctionCapture = EasyMock.newCapture();
        Capture<ToDoubleFunction<Queue>> currentConsumerCountFunctionCapture = EasyMock.newCapture();

        EasyMock.expect(meterRegistry.gauge(
                        EasyMock.eq("rabbit.queue.test_queue.currentMessageCount"),
                        EasyMock.eq(queue),
                        EasyMock.capture(currentMessageCountFunctionCapture)))
                .andReturn(queue);

        EasyMock.expect(meterRegistry.gauge(
                        EasyMock.eq("rabbit.queue.test_queue.currentConsumerCount"),
                        EasyMock.eq(queue),
                        EasyMock.capture(currentConsumerCountFunctionCapture)))
                .andReturn(queue);

        PowerMock.replayAll();
        rabbitQueueMetrics.bindTo(meterRegistry);

        Assert.assertEquals(5432.0, currentMessageCountFunctionCapture.getValue().applyAsDouble(queue), 0.0);
        Assert.assertEquals(2.0, currentConsumerCountFunctionCapture.getValue().applyAsDouble(queue), 0.0);

        PowerMock.verifyAll();
    }

    @Test
    public void testMetricsWithThreshold()
    {
        Queue queue = generateQueue("test.queue");
        rabbitQueueMetrics.addQueue(queue, 10000, 5);
        QueueCheck queueCheck = rabbitQueueMetrics.getQueueChecks().get(0);

        EasyMock.expect(propertiesManager.request(queue)).andAnswer(this::buildQueueProperties).times(2);

        Capture<ToDoubleFunction<Queue>> currentMessageCountFunctionCapture = EasyMock.newCapture();
        Capture<ToDoubleFunction<Queue>> currentConsumerCountFunctionCapture = EasyMock.newCapture();
        Capture<ToDoubleFunction<QueueCheck>> minConsumerCountFunctionCapture = EasyMock.newCapture();
        Capture<ToDoubleFunction<QueueCheck>> maxMessageCountFunctionCapture = EasyMock.newCapture();

        EasyMock.expect(meterRegistry.gauge(
                        EasyMock.eq("rabbit.queue.test_queue.currentMessageCount"),
                        EasyMock.eq(queue),
                        EasyMock.capture(currentMessageCountFunctionCapture)))
                .andReturn(queue);

        EasyMock.expect(meterRegistry.gauge(
                        EasyMock.eq("rabbit.queue.test_queue.currentConsumerCount"),
                        EasyMock.eq(queue),
                        EasyMock.capture(currentConsumerCountFunctionCapture)))
                .andReturn(queue);

        EasyMock.expect(meterRegistry.gauge(
                        EasyMock.eq("rabbit.queue.test_queue.minConsumerCount"),
                        EasyMock.eq(queueCheck),
                        EasyMock.capture(minConsumerCountFunctionCapture)))
                .andReturn(queueCheck);

        EasyMock.expect(meterRegistry.gauge(
                        EasyMock.eq("rabbit.queue.test_queue.maxMessageCount"),
                        EasyMock.eq(queueCheck),
                        EasyMock.capture(maxMessageCountFunctionCapture)))
                .andReturn(queueCheck);

        PowerMock.replayAll();
        rabbitQueueMetrics.bindTo(meterRegistry);

        Assert.assertEquals(5432.0, currentMessageCountFunctionCapture.getValue().applyAsDouble(queue), 0.0);
        Assert.assertEquals(2.0, currentConsumerCountFunctionCapture.getValue().applyAsDouble(queue), 0.0);
        Assert.assertEquals(5.0, minConsumerCountFunctionCapture.getValue().applyAsDouble(queueCheck), 0.0);
        Assert.assertEquals(10000.0, maxMessageCountFunctionCapture.getValue().applyAsDouble(queueCheck), 0.0);

        PowerMock.verifyAll();
    }

    @Test
    public void testMetricsWithException()
    {
        Queue queue = generateQueue("test.queue");
        rabbitQueueMetrics.addQueue(queue, 10000, 5);

        EasyMock.expect(meterRegistry.gauge(
                        EasyMock.eq("rabbit.queue.test_queue.currentMessageCount"),
                        EasyMock.eq(queue),
                        EasyMock.anyObject()))
                .andThrow(new RuntimeException());

        PowerMock.replayAll();
        rabbitQueueMetrics.bindTo(meterRegistry);
        PowerMock.verifyAll();

        // no exception expected
    }

    private RabbitQueueProperties buildQueueProperties()
    {
        RabbitQueueProperties properties = new RabbitQueueProperties();
        properties.setMessageCount(5432);
        properties.setConsumerCount(2);
        return properties;
    }
}