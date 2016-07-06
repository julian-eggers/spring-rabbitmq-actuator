package com.itelg.spring.actuator.rabbitmq;

public class RabbitQueueProperties
{
    private int messageCount;
    private int consumerCount;

    public int getMessageCount()
    {
        return messageCount;
    }

    public void setMessageCount(int messageCount)
    {
        this.messageCount = messageCount;
    }

    public int getConsumerCount()
    {
        return consumerCount;
    }

    public void setConsumerCount(int consumerCount)
    {
        this.consumerCount = consumerCount;
    }

    @Override
    public String toString()
    {
        return "RabbitQueueProperties [messageCount=" + messageCount + ", consumerCount=" + consumerCount + "]";
    }
}