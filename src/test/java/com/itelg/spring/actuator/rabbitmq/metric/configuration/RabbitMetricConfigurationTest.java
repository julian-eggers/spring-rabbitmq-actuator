package com.itelg.spring.actuator.rabbitmq.metric.configuration;

import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.amqp.core.Queue;

import com.itelg.spring.actuator.rabbitmq.metric.RabbitQueueMetrics;
import com.itelg.spring.actuator.rabbitmq.util.QueueUtil;

public class RabbitMetricConfigurationTest
{
    @Test
    public void testRabbitQueueMetrics()
    {
        Queue queue = QueueUtil.generateQueue("test");
        RabbitQueueMetrics metrics = new RabbitMetricConfiguration().rabbitQueueMetrics(Collections.singletonList(queue));
        Assert.assertEquals(1, metrics.getQueueChecks().size());
    }
}