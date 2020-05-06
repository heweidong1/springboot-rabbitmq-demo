package com.kgc.rabbitmqconsumer.util;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.BatchMessageListener;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;


@Component
public class RabbitmqListenerUtil
{


    private int test=0;

    private  Long endOkID=0L;


    @RabbitListener(queues = "testQueue3")
    public void getMessage(Message message)throws Exception
    {
        //能到自定义的订单ID  可以根据这个字段判断 是否是重复消费
        System.out.println(message.getMessageProperties().getHeaders().get("spring_returned_message_correlation"));
        System.out.println("消费者1："+new String(message.getBody(),"UTF-8"));
//        Map map = JSON.parseObject(new String(message.getBody(), "UTF-8"), Map.class);
//        System.out.println(map);
    }


    //通过注解 声明手动确认消息


    public void getMessage2(Message message, Channel channel)throws Exception
    {

//        for(Message message:in)
//        {
//            try
//            {
//                //处理过程
//                System.out.println("消费者2："+new String(message.getBody(),"UTF-8"));
//                //deliveryTag=message.getMessageProperties().getDeliveryTag();
//            }catch (Exception e)
//            {
//                e.printStackTrace();
////                if(deliveryTag!=0L)
////                {
////                    //将以前处理好的消息确认处理
////                    channel.basicAck(deliveryTag,true);
////                }
////
//                //再将错误的消息退回
//                //批量退回 参数1：消息的唯一标识  参数二：是否批量退回 参数三：是否重新回到消息队列
//                channel.basicNack(message.getMessageProperties().getDeliveryTag(),false,true);
//            }
//            if((Boolean) message.getMessageProperties().getHeaders().get("AmqpHeaders.LAST_IN_BATCH"))
//            {
//                channel.basicAck(message.getMessageProperties().getDeliveryTag(),true);
//            }
//        }







        //参数1：消息的唯一标识  参数二：是否是批量确认
        //消费者发送消息已经处理完毕消息了

//        if(isTrue%2==0?true:false)
//        {
//            //消息确认
//            System.out.println("消费者2："+new String(message.getBody(),"UTF-8"));
//            //批量确认
//            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
//        }else
//        {
//            //消息退回
//            //批量退回 参数1：消息的唯一标识  参数二：是否批量退回 参数三：是否重新回到消息队列
//            channel.basicNack(message.getMessageProperties().getDeliveryTag(),false,true);
//            //单条退回  参数一：消息的唯一标识  参数二：是否回到消息队列
//            //channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
//            isTrue++;
//        }

    }

    @RabbitListener(queues = "testQueue2",containerFactory = "simpleRabbitListenerContainerFactory2")
    public void onMessageBatch(List<org.springframework.messaging.Message> messages ,Channel channel) {
        Iterator<org.springframework.messaging.Message> iterator = messages.iterator();

        while (iterator.hasNext())
        {
            org.springframework.messaging.Message next = iterator.next();
            test++;
            try {

                if(test%5==0)
                {
                    System.out.println(1/0);
                }
                //业务逻辑
                System.out.println(next.getPayload());
                endOkID=(Long) next.getHeaders().get("amqp_deliveryTag");

            }catch (Exception e)
            {
                System.out.println("错误信息："+next.getPayload());
                try {
                    channel.basicNack((Long) next.getHeaders().get("amqp_deliveryTag"),false,false);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                continue;
            }
        }

        try {
            //批量确认
            channel.basicAck(endOkID,true);
            endOkID=0L;

        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(messages.size());
    }


    @RabbitListener(queues = "deadQueue")
    public void getMessage4(Message message)throws Exception
    {
        //能到自定义的订单ID  可以根据这个字段判断 是否是重复消费
        //System.out.println(message.getMessageProperties().getHeaders().get("spring_returned_message_correlation"));
        System.err.println("死信消费者："+new String(message.getBody(),"UTF-8"));
//        Map map = JSON.parseObject(new String(message.getBody(), "UTF-8"), Map.class);
//        System.out.println(map);
    }
}
