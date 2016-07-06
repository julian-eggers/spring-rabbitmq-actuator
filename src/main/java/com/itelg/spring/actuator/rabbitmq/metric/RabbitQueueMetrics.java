package com.itelg.spring.actuator.rabbitmq.metric;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Queue;
import org.springframework.boot.actuate.endpoint.PublicMetrics;
import org.springframework.boot.actuate.metrics.Metric;

import com.itelg.spring.actuator.rabbitmq.QueueCheck;
import com.itelg.spring.actuator.rabbitmq.RabbitQueueProperties;
import com.itelg.spring.actuator.rabbitmq.RabbitQueuePropertiesManager;

public class RabbitQueueMetrics implements PublicMetrics
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
    public Collection<Metric<?>> metrics()
    {
        List<Metric<?>> metrics = new ArrayList<>();

        for (QueueCheck queueCheck : queueChecks)
        {
            try
            {
                RabbitQueueProperties queueProperties = propertiesManager.request(queueCheck.getQueue());
                String queueName = queueCheck.getQueue().getName().replace(".", "_");
                int maxMessageCount = queueCheck.getMaxMessageCount();
                int minConsumerCount = queueCheck.getMinConsumerCount();

                int currentMessageCount = queueProperties.getMessageCount();
                metrics.add(new Metric<Number>("rabbit.queue." + queueName + ".currentMessageCount", currentMessageCount));

                int currentConsumerCount = queueProperties.getConsumerCount();
                metrics.add(new Metric<Number>("rabbit.queue." + queueName + ".currentConsumerCount", currentConsumerCount));

                if (maxMessageCount > 0)
                {
                    metrics.add(new Metric<Number>("rabbit.queue." + queueName + ".maxMessageCount", maxMessageCount));
                }

                if (minConsumerCount > 0)
                {
                    metrics.add(new Metric<Number>("rabbit.queue." + queueName + ".minConsumerCount", minConsumerCount));
                }
            }
            catch (Exception e)
            {
                log.error(e.getMessage(), e);
            }
        }

        return metrics;
    }

    public List<QueueCheck> getQueueChecks()
    {
        return queueChecks;
    }
}