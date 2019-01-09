package com.itelg.spring.actuator.rabbitmq.metric;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Queue;

import com.itelg.spring.actuator.rabbitmq.QueueCheck;
import com.itelg.spring.actuator.rabbitmq.RabbitQueuePropertiesManager;

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
            try
            {
                Queue queue = queueCheck.getQueue();
                String queueName = queueCheck.getQueue().getName().replace(".", "_");
                int maxMessageCount = queueCheck.getMaxMessageCount();
                int minConsumerCount = queueCheck.getMinConsumerCount();

                meterRegistry.gauge(
                        "rabbit.queue." + queueName + ".currentMessageCount",
                        queue,
                        q -> propertiesManager.request(q).getMessageCount());

                meterRegistry.gauge(
                        "rabbit.queue." + queueName + ".currentConsumerCount",
                        queue,
                        q -> propertiesManager.request(q).getConsumerCount());

                if (maxMessageCount > 0)
                {
                    meterRegistry.gauge(
                            "rabbit.queue." + queueName + ".maxMessageCount",
                            queueCheck,
                            QueueCheck::getMaxMessageCount);
                }

                if (minConsumerCount > 0) {
                    meterRegistry.gauge(
                            "rabbit.queue." + queueName + ".minConsumerCount",
                            queueCheck,
                            QueueCheck::getMinConsumerCount);
                }
            }
            catch (Exception e)
            {
                log.error(e.getMessage(), e);
            }
        }
    }

    public List<QueueCheck> getQueueChecks()
    {
        return queueChecks;
    }
}