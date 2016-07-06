package com.itelg.spring.actuator.rabbitmq.metric.configuration;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

@Retention(RUNTIME)
@Target(TYPE)
@Import(RabbitMetricConfiguration.class)
public @interface EnableRabbitMetrics
{

}