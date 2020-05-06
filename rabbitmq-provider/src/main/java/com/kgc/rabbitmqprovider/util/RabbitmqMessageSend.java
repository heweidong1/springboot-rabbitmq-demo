package com.kgc.rabbitmqprovider.util;


import com.alibaba.fastjson.JSON;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class RabbitmqMessageSend
{
    @Autowired
    RabbitTemplate rabbitTemplate;

    public void senMessage(String message,String routingkey,String exchangename) throws InterruptedException {
        //转换并且发送 CorrelationData设置该消息的唯一表示，当发送成功或者失败
        //回调的时候 会将这个消息的唯一表示床送过过来
        CorrelationData correlationData = new CorrelationData();
        correlationData.setId("订单ID");
        Map<String,Object> map =new HashMap<>();
        map.put("name","123");
        map.put("password","123456");
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setContentType("text/plain");
//        rabbitTemplate.convertAndSend("exchange2","init.order",map,correlationData);
        for(int i=1;i<=20;i++)
        {
            Message message1 = new Message(JSON.toJSONBytes("hello"+i),messageProperties);
            rabbitTemplate.convertAndSend("exchange2","init.order","hello"+i,correlationData);
        }

    }

}
