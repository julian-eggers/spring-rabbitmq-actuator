package com.itelg.spring.actuator.rabbitmq;

import org.junit.Assert;
import org.junit.Test;

import com.itelg.spring.actuator.rabbitmq.RabbitQueueProperties;

public class RabbitQueuePropertiesTest
{
	@Test
	public void testToString()
	{
		Assert.assertTrue(new RabbitQueueProperties().toString().startsWith("RabbitQueueProperties"));
	}
}