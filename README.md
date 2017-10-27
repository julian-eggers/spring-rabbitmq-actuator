spring-rabbitmq-actuator
========================

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.itelg.spring/spring-rabbitmq-actuator/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.itelg.spring/spring-rabbitmq-actuator)
[![Codacy Badge](https://api.codacy.com/project/badge/grade/ab6ef73712914dabac91965fe49eb297)](https://www.codacy.com/app/eggers-julian/spring-rabbitmq-actuator)
[![Coverage Status](https://coveralls.io/repos/julian-eggers/spring-rabbitmq-actuator/badge.svg?branch=master&service=github)](https://coveralls.io/github/julian-eggers/spring-rabbitmq-actuator?branch=master)
[![Build Status](https://travis-ci.org/julian-eggers/spring-rabbitmq-actuator.svg?branch=master)](https://travis-ci.org/julian-eggers/spring-rabbitmq-actuator)

SpringBoot RabbitMQ Actuator (Queue Metrics & Health-Checks)

#### Maven
```xml
<dependency>
	<groupId>com.itelg.spring</groupId>
	<artifactId>spring-rabbitmq-actuator</artifactId>
	<version>0.5.0-RELEASE</version>
</dependency>
```

#### Precondition ([Example](https://github.com/julian-eggers/spring-rabbitmq-dlx-example/blob/master/src/main/java/com/itelg/spring/rabbitmq/example/configuration/RabbitConfiguration.java#L98))
You have to add the declaring RabbitAdmin to each queue.
The specific RabbitAdmin is required to fetch the queue-information.
```java
@Bean
public Queue exampleQueue()
{
	Queue queue = new Queue("com.itelg.spring.rabbitmq.test");
	queue.setAdminsThatShouldDeclare(rabbitAdmin());
	return queue;
}
```


## Health-Checks

#### Example
```java
@Bean
public HealthIndicator rabbitQueueCheckHealthIndicator()
{
	RabbitQueueCheckHealthIndicator healthIndicator = new RabbitQueueCheckHealthIndicator();
	healthIndicator.addQueueCheck(exampleQueue1, 10000, 1);
	healthIndicator.addQueueCheck(exampleQueue2, 50000, 3);
	return healthIndicator;
}
```

#### Response ([health.json](http://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-endpoints.html#production-ready-health))
```json
{
	"status" : "DOWN",
	"rabbitQueueCheck" : 
	{
		"status" : "DOWN",
		"com.examle.exampleQueue1" : 
		{
			"status" : "UP",
			"currentMessageCount" : 214,
			"maxMessageCount" : 10000,
			"currentConsumerCount" : 5,
			"minConsumerCount" : 1
		},
		"com.example.exampleQueue2" : 
		{
			"status" : "DOWN",
			"currentMessageCount" : 67377,
			"maxMessageCount" : 50000,
			"currentConsumerCount" : 0,
			"minConsumerCount" : 3
		}
	}
}
```


## Metrics

#### Example (Autowires all queue-beans)
```java
@EnableRabbitMetrics
@Configuration
public class RabbitMetricsConfiguration
{
}
```

#### Response ([metrics.json](http://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-metrics.html))
```json
{
  "mem" : 626258,
  "mem.free" : 460251,
  "processors" : 16,
  "instance.uptime" : 17148451,
...
  "rabbit.queue.com_examle_exampleQueue1.currentMessageCount" : 214,
  "rabbit.queue.com_examle_exampleQueue1.currentConsumerCount" : 5,
  "rabbit.queue.com_examle_exampleQueue2.currentMessageCount" : 67377,
  "rabbit.queue.com_examle_exampleQueue2.currentConsumerCount" : 0,
...
}
```
