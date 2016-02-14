package com.itelg.spring.actuator.rabbitmq;

import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.api.easymock.annotation.Mock;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Health.Builder;

import com.itelg.spring.actuator.rabbitmq.RabbitQueueCheckHealthIndicator;
import com.itelg.spring.actuator.rabbitmq.RabbitQueueProperties;
import com.itelg.spring.actuator.rabbitmq.RabbitQueuePropertiesManager;

import org.springframework.boot.actuate.health.Status;

@RunWith(PowerMockRunner.class)
public class RabbitQueueCheckHealthIndicatorTest
{
	private RabbitQueueCheckHealthIndicator healthIndicator;
	
	@Mock
	private RabbitQueuePropertiesManager propertiesManager;
	
	@Before
	public void before()
	{
		healthIndicator = new RabbitQueueCheckHealthIndicator();
		Whitebox.setInternalState(healthIndicator, propertiesManager);
	}
	
	@Test
	public void testDoHealthCheckNoQueueChecks() throws Exception
	{
		Builder builder = new Builder(Status.OUT_OF_SERVICE);
		healthIndicator.doHealthCheck(builder);
		Assert.assertEquals(Status.UP, builder.build().getStatus());
	}
	
	@SuppressWarnings({ "unchecked", "boxing" })
	@Test
	public void testDoHealthCheckSingleQueueCheckUp() throws Exception
	{
		Queue queue = generateQueue("test");
		healthIndicator.addQueueCheck(queue, 10000, 2);
		
		RabbitQueueProperties properties = new RabbitQueueProperties();
		properties.setConsumerCount(4);
		properties.setMessageCount(5883);
		propertiesManager.request(queue);
		PowerMock.expectLastCall().andReturn(properties);
		
		PowerMock.replayAll();
		Builder builder = new Builder(Status.OUT_OF_SERVICE);
		healthIndicator.doHealthCheck(builder);
		PowerMock.verifyAll();
		
		Health health = builder.build();
		Assert.assertEquals(Status.UP, health.getStatus());
		Assert.assertNotNull(health.getDetails().get("test"));
		Map<String, Object> details = (Map<String, Object>) health.getDetails().get("test");
		Assert.assertEquals(Status.UP.getCode(), details.get("status"));
		Assert.assertEquals(5883, details.get("currentMessageCount"));
		Assert.assertEquals(10000, details.get("maxMessageCount"));
		Assert.assertEquals(4, details.get("currentConsumerCount"));
		Assert.assertEquals(2, details.get("minConsumerCount"));
	}
	
	@SuppressWarnings({ "unchecked", "boxing" })
	@Test
	public void testDoHealthCheckSingleQueueCheckQueueSizeDown() throws Exception
	{
		Queue queue = generateQueue("test");
		healthIndicator.addQueueCheck(queue, 10000, 2);
		
		RabbitQueueProperties properties = new RabbitQueueProperties();
		properties.setConsumerCount(4);
		properties.setMessageCount(15883);
		propertiesManager.request(queue);
		PowerMock.expectLastCall().andReturn(properties);
		
		PowerMock.replayAll();
		Builder builder = new Builder(Status.OUT_OF_SERVICE);
		healthIndicator.doHealthCheck(builder);
		PowerMock.verifyAll();
		
		Health health = builder.build();
		Assert.assertEquals(Status.DOWN, health.getStatus());
		Assert.assertNotNull(health.getDetails().get("test"));
		Map<String, Object> details = (Map<String, Object>) health.getDetails().get("test");
		Assert.assertEquals(Status.DOWN.getCode(), details.get("status"));
		Assert.assertEquals(15883, details.get("currentMessageCount"));
		Assert.assertEquals(10000, details.get("maxMessageCount"));
		Assert.assertEquals(4, details.get("currentConsumerCount"));
		Assert.assertEquals(2, details.get("minConsumerCount"));
	}
	
	@SuppressWarnings({ "unchecked", "boxing" })
	@Test
	public void testDoHealthCheckSingleQueueCheckConsumerDown() throws Exception
	{
		Queue queue = generateQueue("test");
		healthIndicator.addQueueCheck(queue, 10000, 2);
		
		RabbitQueueProperties properties = new RabbitQueueProperties();
		properties.setConsumerCount(1);
		properties.setMessageCount(5883);
		propertiesManager.request(queue);
		PowerMock.expectLastCall().andReturn(properties);
		
		PowerMock.replayAll();
		Builder builder = new Builder(Status.OUT_OF_SERVICE);
		healthIndicator.doHealthCheck(builder);
		PowerMock.verifyAll();
		
		Health health = builder.build();
		Assert.assertEquals(Status.DOWN, health.getStatus());
		Assert.assertNotNull(health.getDetails().get("test"));
		Map<String, Object> details = (Map<String, Object>) health.getDetails().get("test");
		Assert.assertEquals(Status.DOWN.getCode(), details.get("status"));
		Assert.assertEquals(5883, details.get("currentMessageCount"));
		Assert.assertEquals(10000, details.get("maxMessageCount"));
		Assert.assertEquals(1, details.get("currentConsumerCount"));
		Assert.assertEquals(2, details.get("minConsumerCount"));
	}
	
	@Test
	public void testDoHealthCheckSingleQueueCheckMetricException() throws Exception
	{
		Queue queue = generateQueue("test");
		healthIndicator.addQueueCheck(queue, 10000, 2);
		
		propertiesManager.request(queue);
		PowerMock.expectLastCall().andThrow(new RuntimeException());
		
		PowerMock.replayAll();
		Builder builder = new Builder(Status.OUT_OF_SERVICE);
		healthIndicator.doHealthCheck(builder);
		PowerMock.verifyAll();
		
		Health health = builder.build();
		Assert.assertEquals(Status.DOWN, health.getStatus());
		Assert.assertNull(health.getDetails().get("test"));
	}
	
	@SuppressWarnings({ "unchecked", "boxing" })
	@Test
	public void testDoHealthCheckMultipleQueueChecksOneUpOneDown() throws Exception
	{
		Queue queue1 = generateQueue("test1");
		healthIndicator.addQueueCheck(queue1, 10000, 2);
		Queue queue2 = generateQueue("test2");
		healthIndicator.addQueueCheck(queue2, 40000, 5);
		
		RabbitQueueProperties properties1 = new RabbitQueueProperties();
		properties1.setConsumerCount(1);
		properties1.setMessageCount(15883);
		propertiesManager.request(queue1);
		PowerMock.expectLastCall().andReturn(properties1);
		
		RabbitQueueProperties properties2 = new RabbitQueueProperties();
		properties2.setConsumerCount(10);
		properties2.setMessageCount(5883);
		propertiesManager.request(queue2);
		PowerMock.expectLastCall().andReturn(properties2);
		
		PowerMock.replayAll();
		Builder builder = new Builder(Status.OUT_OF_SERVICE);
		healthIndicator.doHealthCheck(builder);
		PowerMock.verifyAll();
		
		Health health = builder.build();
		Assert.assertEquals(Status.DOWN, health.getStatus());
		Assert.assertEquals(2, health.getDetails().size());
		
		Assert.assertNotNull(health.getDetails().get("test1"));
		Map<String, Object> details1 = (Map<String, Object>) health.getDetails().get("test1");
		Assert.assertEquals(Status.DOWN.getCode(), details1.get("status"));
		Assert.assertEquals(15883, details1.get("currentMessageCount"));
		Assert.assertEquals(10000, details1.get("maxMessageCount"));
		Assert.assertEquals(1, details1.get("currentConsumerCount"));
		Assert.assertEquals(2, details1.get("minConsumerCount"));
		
		Assert.assertNotNull(health.getDetails().get("test2"));
		Map<String, Object> details2 = (Map<String, Object>) health.getDetails().get("test2");
		Assert.assertEquals(Status.UP.getCode(), details2.get("status"));
		Assert.assertEquals(5883, details2.get("currentMessageCount"));
		Assert.assertEquals(40000, details2.get("maxMessageCount"));
		Assert.assertEquals(10, details2.get("currentConsumerCount"));
		Assert.assertEquals(5, details2.get("minConsumerCount"));
	}
	
	@Test
	public void testAddQueueCheckMaxMessageCountAndMinConsumerCount()
	{
		healthIndicator.addQueueCheck(generateQueue("test"), 10000, 5);
		healthIndicator.addQueueCheck(generateQueue("test"), 5000, 2);
		Assert.assertEquals(2, healthIndicator.getQueueChecks().size());
	}
	
	@Test
	public void testAddQueueCheckMaxMessageCount()
	{
		healthIndicator.addQueueCheck(generateQueue("test"), 10000);
		healthIndicator.addQueueCheck(generateQueue("test"), 5000);
		Assert.assertEquals(2, healthIndicator.getQueueChecks().size());
	}
	
	@Test
	public void testGetQueueChecks()
	{
		Assert.assertNotNull(healthIndicator.getQueueChecks());
		
		Queue queue = generateQueue("test");
		healthIndicator.addQueueCheck(queue, 1);
		healthIndicator.addQueueCheck(queue, 1);
		Assert.assertEquals(2, healthIndicator.getQueueChecks().size());
	}
	
	private Queue generateQueue(String name)
	{
		CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
		RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
		
		Queue queue = new Queue(name);
		queue.setAdminsThatShouldDeclare(rabbitAdmin);
		
		return queue;
	}
}