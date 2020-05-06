package com.kgc.rabbitmqprovider.config;

import com.alibaba.fastjson.JSON;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Component
public class RabbitmqConfig
{
    @Autowired
    RabbitAdmin rabbitAdmin;
    @Bean
    public ConnectionFactory connectionFactory()
    {
        //创建连接工厂
        CachingConnectionFactory connectionFactory =
                new CachingConnectionFactory("localhost",5672);
        connectionFactory.setUsername("test");
        connectionFactory.setPassword("test");
        connectionFactory.setVirtualHost("/test");
        //开启消息发送方确认机制，当消息发送到rabbitmq交换机的时候会回调本地的方法
        //如果设置交换机发送消息到队列错误回调，也要设置这个属性
        connectionFactory.setPublisherConfirms(true);


        return connectionFactory;
    }

    @Bean
    public RabbitTemplate rabbitTemplate()
    {
        RabbitTemplate rabbitTemplate = new RabbitTemplate();
        rabbitTemplate.setConnectionFactory( connectionFactory());
//        rabbitTemplate.setConfirmCallback((correlationData,ack,cause)->{
//
//        });
        //开启消息发送方确认机制，当消息发送到rabbitmq的时候会回调本地的方法
        //当发送成功 ack=true  cause=null  correlationData测试发送的时候设置的唯一表示
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                System.out.println(ack);
                System.out.println(cause);
                System.out.println(correlationData);
            }
        });
        //设置失败回调，交换机发送消息到队列时候的错误回调
        rabbitTemplate.setMandatory(true);
        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            @Override
            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
                //message  发送的消息+发送的消息配置【过期时间、权重】
                System.out.println(message);
                //状态码
                System.out.println(replyCode);
                //replyText 失败参数
                System.out.println(replyText);
                //该条消息发送到的交换机
                System.out.println(exchange);
                //routingKey 该消息的路邮件是什么
                System.out.println(routingKey);

            }
        });


        rabbitTemplate.setMessageConverter(new MessageConverter() {
            //发送的时候装换成什么样子
            @Override
            public Message toMessage(Object o, MessageProperties messageProperties) throws MessageConversionException {
                //配置消费者乱码问题
                messageProperties.setContentType("text/plain");
                Message message=new Message(JSON.toJSONBytes(o),messageProperties);
                System.out.println("调用了消息解析器");
                return message;
            }
            //接收的时候装换成什么样子
            @Override
            public Object fromMessage(Message message) throws MessageConversionException {
                return null;
            }
        });
        //设置消息装换器
        return rabbitTemplate;
    }




    //springboot项目创建交换机  队列等操作
    //------------------------------------------------------------------------------------------
    @Bean
    public DirectExchange directExchange()
    {
        //绑定备用交换机，当消息在exchange2交换机中没有发送到队列的时候，会将这个消息发送到
        //备用交换机中，别用交换机将消息发送到备用交换机绑定的队列中，这个时候会调用消息确认机制
        // 【发送到交换机时 调用的回调】
        //但不会调用失败回调【没有发送到队列时调用的回调，当备用交换机没有发送到队列的时候，会调用】
        Map<String,Object> map =new HashMap<>();
        map.put("alternate-exchange","directExchange");
        return new DirectExchange("exchange2",false,false,map);
    }

    //定义死信交换机
    @Bean
    public DirectExchange deadExchange()
    {
        return new DirectExchange("deadExchange");
    }

    //声明死信队列
    @Bean
    public Queue deadQueue()
    {
        //名字 是否持久化
        return new Queue("deadQueue",true,false,false);
    }
    @Bean
    public Queue queue()
    {
        Map<String,Object> map =new HashMap<>();
        //对这个队列声明一个死信交换机，当有错误消息会自动重定向到这个交换机中
        map.put("x-dead-letter-exchange","deadExchange");
        //发生错误的消息本来的routing-key和死信队列的key不匹配，所以要将原来的key修改
        map.put("x-dead-letter-routing-key","dead.news");
        //名字 是否持久化
        return new Queue("testQueue2",true,false,false,map);
    }

    @Bean
    public Binding binding()
    {
        //绑定一个队列 to: 绑定到哪个交换机上面 with：绑定的路由建（routingKey）
        return BindingBuilder.bind(queue()).to(directExchange()).with("init.order");
    }

    //绑定死信交换机
    @Bean
    public Binding binDingDead()
    {
        //绑定一个队列 to: 绑定到哪个交换机上面 with：绑定的路由建（routingKey）
        return BindingBuilder.bind(deadQueue()).to(deadExchange()).with("dead.news");

    }

    //创建初始化RabbitAdmin对象

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
        // 只有设置为 true，spring 才会加载 RabbitAdmin 这个类
        rabbitAdmin.setAutoStartup(true);
        return rabbitAdmin;
    }
    //创建交换机和对列
    @Bean
    public void createExchangeQueue () {
        rabbitAdmin.declareExchange(directExchange());
        rabbitAdmin.declareExchange(deadExchange());
        rabbitAdmin.declareQueue(deadQueue());
        rabbitAdmin.declareQueue(queue());
    }
    //--------------------------------------------------------------------------------------------









}
