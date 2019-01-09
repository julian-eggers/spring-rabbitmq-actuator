package com.itelg.spring.actuator.rabbitmq.metric;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Queue;

import com.itelg.spring.actuator.rabbitmq.QueueCheck;
import com.itelg.spring.actuator.rabbitmq.RabbitQueuePropertiesManager;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.MeterBinder;

public class RabbitQueueMetrics implements MeterBinder
{
    private static final Logger log = LoggerFactory.getLogger(RabbitQueueMetrics.class);
    private RabbitQueuePropertiesManager propertiesManager = new RabbitQueuePropertiesManager();
    private List<QueueCheck> queueChecks = new ArrayList<>();

    public RabbitQueueMetrics(List<Queue> queues)
    {
        for (Queue queue : queues)
        {
            addQueue(queue);
        }
    }

    public RabbitQueueMetrics()
    {
    }

    public void addQueue(Queue queue)
    {
        queueChecks.add(new QueueCheck(queue, 0, 0));
    }

    public void addQueue(Queue queue, int maxMessageCount)
    {
        queueChecks.add(new QueueCheck(queue, maxMessageCount, 0));
    }

    public void addQueue(Queue queue, int maxMessageCount, int minConsumerCount)
    {
        queueChecks.add(new QueueCheck(queue, maxMessageCount, minConsumerCount));
    }

    @Override
    public void bindTo(MeterRegistry meterRegistry)
    {
        for (QueueCheck queueCheck : queueChecks)
        {
            Queue queue = queueCheck.getQueue();
            Tags tags = Tags.of("queue", queue.getName());

            try
            {
                meterRegistry.gauge("rabbitmq.queue.messages.current", tags, queue, q -> propertiesManager.request(q).getMessageCount());
                meterRegistry.gauge("rabbitmq.queue.consumers.current", tags, queue, q -> propertiesManager.request(q).getConsumerCount());

                if (queueCheck.getMaxMessageCount() > 0)
                {
                    meterRegistry.gauge("rabbitmq.queue.messages.max", tags, queueCheck, QueueCheck::getMaxMessageCount);
                }

                if (queueCheck.getMinConsumerCount() > 0)
                {
                    meterRegistry.gauge("rabbitmq.queue.consumers.min", tags, queueCheck, QueueCheck::getMinConsumerCount);
                }
            }
            catch (Exception e)
            {
                log.warn("Failed to fetch queue-information for {}", queue.getName(), e);
            }
        }
    }

    public List<QueueCheck> getQueueChecks()
    {
        return queueChecks;
    }
}
