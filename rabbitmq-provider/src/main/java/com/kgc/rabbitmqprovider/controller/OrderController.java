package com.kgc.rabbitmqprovider.controller;

import com.alibaba.fastjson.JSON;
import com.kgc.rabbitmqprovider.util.RabbitmqMessageSend;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;


@RestController
public class OrderController {

    @Autowired
    RabbitmqMessageSend rabbitmqMessageSend;

    @RequestMapping("/oder.do")
    public Object oder() throws InterruptedException {
        Map<String,Object> map =new HashMap<>();
        map.put("name","123");
        map.put("password","123456");
        String s = JSON.toJSONString(map);
        rabbitmqMessageSend.senMessage(s, "init.order", "exchange2");
        return "下单成功";
    }

}
