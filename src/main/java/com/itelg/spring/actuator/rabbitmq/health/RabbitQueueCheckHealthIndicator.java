package com.itelg.spring.actuator.rabbitmq.health;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Queue;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health.Builder;

import com.itelg.spring.actuator.rabbitmq.QueueCheck;
import com.itelg.spring.actuator.rabbitmq.RabbitQueueProperties;
import com.itelg.spring.actuator.rabbitmq.RabbitQueuePropertiesManager;

import org.springframework.boot.actuate.health.Status;

public class RabbitQueueCheckHealthIndicator extends AbstractHealthIndicator
{
    private static final Logger log = LoggerFactory.getLogger(RabbitQueueCheckHealthIndicator.class);
    private List<QueueCheck> queueChecks = new ArrayList<QueueCheck>();
    private RabbitQueuePropertiesManager propertiesManager = new RabbitQueuePropertiesManager();

    @Override
    protected void doHealthCheck(Builder builder) throws Exception
    {
        builder.up();

        for (QueueCheck queueCheck : queueChecks)
        {
            try
            {
                RabbitQueueProperties queueProperties = propertiesManager.request(queueCheck.getQueue());
                String queueName = queueCheck.getQueue().getName();
                int currentMessageCount = queueProperties.getMessageCount();
                int maxMessageCount = queueCheck.getMaxMessageCount();
                int currentConsumerCount = queueProperties.getConsumerCount();
                int minConsumerCount = queueCheck.getMinConsumerCount();

                Map<String, Object> details = new LinkedHashMap<String, Object>();
                details.put("status", Status.UP.getCode());
                details.put("currentMessageCount", Integer.valueOf(currentMessageCount));
                details.put("maxMessageCount", Integer.valueOf(maxMessageCount));
                details.put("currentConsumerCount", Integer.valueOf(currentConsumerCount));
                details.put("minConsumerCount", Integer.valueOf(minConsumerCount));
                builder.withDetail(queueName, details);

                if (currentMessageCount > maxMessageCount)
                {
                    builder.down();
                    details.put("status", Status.DOWN.getCode());
                    log.warn(queueName + ": Too many messages ready (Current: " + currentMessageCount + ", " + "Max-Messages: " + queueCheck.getMaxMessageCount() + ")");
                }

                if (currentConsumerCount < minConsumerCount)
                {
                    builder.down();
                    details.put("status", Status.DOWN.getCode());
                    log.warn(queueName + ": Not enough consumers active (Current: " + currentConsumerCount + ", " + "Min-Consumers: " + queueCheck.getMinConsumerCount() + ")");
                }
            }
            catch (Exception e)
            {
                log.error(e.getMessage(), e);
                builder.down();
            }
        }
    }

    public void addQueueCheck(Queue queue, int maxMessageCount)
    {
        queueChecks.add(new QueueCheck(queue, maxMessageCount, 1));
    }

    public void addQueueCheck(Queue queue, int maxMessageCount, int minConsumerCount)
    {
        queueChecks.add(new QueueCheck(queue, maxMessageCount, minConsumerCount));
    }

    public List<QueueCheck> getQueueChecks()
    {
        return queueChecks;
    }
}