package com.hmdp.mq;

import com.hmdp.entity.VoucherOrder;
import com.hmdp.utils.MQConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MqSender {

    @Autowired
    RabbitTemplate rabbitTemplate;

    public void sendSeckillMessage(VoucherOrder voucherOrder, boolean reliable) {
        log.info("发送消息：" + voucherOrder);
        if (reliable) {
            // 定义确认回调机制，当 publisher 将消息发送到 exchange 失败，则重新发送一次
            rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
                if (!ack) { // 如果发送失败，则重新再次发送
                    log.error("消息发送失败，错误原因：{}，再次发送。", cause);
                    // 重新发送消息
                    rabbitTemplate.convertAndSend(MQConstants.SECKILL_EXCHANGE, MQConstants.SECKILL_ROUTING_KEY,
                            voucherOrder); //, new CorrelationData(voucherOrder.getId().toString())
                }
            });
            // 设置交换机处理消息的模式
            rabbitTemplate.setMandatory(true);
            // 设置退回函数，当 exchange 将消息发送到队列失败时，会自动将消息退回给 publisher
            rabbitTemplate.setReturnCallback((message, replyCode, replyText, exchange, routingKey) ->
                    log.error("交换机发送消息到队列失败，错误原因：{}，执行将消息退回到 publisher 操作。", replyText));
        }
        // 发送消息（默认为消息持久化）
        log.info("准备发送秒杀消息到 MQ，订单信息: {}", voucherOrder);
        rabbitTemplate.convertAndSend(MQConstants.SECKILL_EXCHANGE, MQConstants.SECKILL_ROUTING_KEY, voucherOrder);
        log.info("消息已发送到 MQ，routingKey: {}", MQConstants.SECKILL_ROUTING_KEY);

    }

}

