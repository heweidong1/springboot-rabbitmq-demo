package com.kgc.rabbitmqconsumer.config;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.SimpleMessageConverter;

import java.util.List;

@Configuration

public class RabbitmqConfig
{
    @Bean
    public ConnectionFactory connectionFactory()
    {
        //创建连接工厂
        CachingConnectionFactory connectionFactory =
                new CachingConnectionFactory("localhost",5672);
        connectionFactory.setUsername("test");
        connectionFactory.setPassword("test");
        connectionFactory.setVirtualHost("/test");


        return connectionFactory;
    }

    @Bean
    public RabbitTemplate rabbitTemplate()
    {
        RabbitTemplate rabbitTemplate = new RabbitTemplate();
        rabbitTemplate.setConnectionFactory( connectionFactory());

        return rabbitTemplate;
    }

    //使用bean的方式声明一个消费者 老版本

    public SimpleMessageListenerContainer simpleMessageListenerContainer()
    {
        SimpleMessageListenerContainer smlc = new SimpleMessageListenerContainer();
        //设置连接工厂
        smlc.setConnectionFactory(connectionFactory());
//        smlc.setMessageListener(new MessageListener() {
//            @Override
//            public void onMessage(Message message) {
//
//            }
//        });
        smlc.setMessageListener((message)->{

        });
        //声明监听哪个队列
        smlc.addQueueNames("testQueue2");
        //设置手动确认  默认是手动确认，意思就是当我们消费消息的时候
        //当消费完毕消息后 ，要告诉rabbitmq我消费完了，当rabbitmq收到消息后
        //会彻底将这个消息清除掉
        smlc.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        return smlc;
    }

    //利用注解声明消息确认  需要的组件

    @Bean
    public SimpleRabbitListenerContainerFactory simpleRabbitListenerContainerFactory2()
    {
        SimpleRabbitListenerContainerFactory srlcf = new SimpleRabbitListenerContainerFactory();
        srlcf.setConnectionFactory(connectionFactory());
        //AcknowledgeMode.MANUAL  手动确认
        //AcknowledgeMode.AUTO  自动确认 当收到消息就发送 已经消费了，不安全
        //AcknowledgeMode.NONE  不确认
        srlcf.setAcknowledgeMode(AcknowledgeMode.MANUAL);

        //设置消息预取的数量
        //消息预取，必须开启 手动确认
        //如果预取了500条  ，其中一条发生错误，需要将这条消息退回，但是在退回这条消息的时候，必须
        //将这条消息前边处理的消息  批量确认，才可以退回
        //我们可以将没有发生错误的消息的唯一标识保存，等发生错误在批量确认。
        srlcf.setPrefetchCount(10);
        //将多条消息合并一条
        srlcf.setBatchListener(true);
        srlcf.setConsumerBatchEnabled(true);
        //大小，10条为一条信息
        srlcf.setBatchSize(10);

        return srlcf;

    }













}
