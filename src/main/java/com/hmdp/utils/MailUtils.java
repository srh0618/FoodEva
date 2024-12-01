package com.hmdp.utils;

import com.sun.mail.util.MailSSLSocketFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;

import static com.hmdp.utils.RedisConstants.MAX_SENDS;

@Component
public class MailUtils {
    private final StringRedisTemplate stringRedisTemplate;

    private static final long TIME_LIMIT = TimeUnit.MINUTES.toMillis(10);  // 10分钟的时间限制
    public MailUtils(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public boolean canSendCode(String email) {
        long currentTime = System.currentTimeMillis();
        String key = "emailSendHistory:" + email;  // 使用邮箱地址作为 key

        // 获取 Redis 中的 ListOperations
        ListOperations<String, String> listOps = stringRedisTemplate.opsForList();

        // 获取该邮箱的发送记录（存储在 Redis 的列表中）
        List<String> sendHistory = listOps.range(key, 0, -1);

        // 清理超过10分钟的记录
        if (sendHistory != null) {
            sendHistory.removeIf(timestamp -> currentTime - Long.parseLong(timestamp) > TIME_LIMIT);
        }

        // 判断该用户在过去10分钟内是否已经发送了3条验证码
        if (sendHistory == null || sendHistory.size() < MAX_SENDS) {
            // 将当前时间戳添加到 Redis 列表
            listOps.rightPush(key, String.valueOf(currentTime));
            stringRedisTemplate.expire(key, TIME_LIMIT, TimeUnit.MILLISECONDS);  // 设置 key 的过期时间为10分钟
            return true;  // 可以发送验证码
        }

        return false;  // 超过限制，不能发送
    }

    public void sendTestMail(String email, String code) throws MessagingException, GeneralSecurityException {
        // 如果不能发送验证码，则返回
        if (!canSendCode(email)) {
            System.out.println("10分钟内最多发送3条验证码，请稍后再试.");
            return;
        }
        // 创建Properties 类用于记录邮箱的一些属性
        Properties props = new Properties();
        // 表示SMTP发送邮件，必须进行身份验证
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2"); // 指定SSL版本
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
//        props.put("mail.smtp.starttls.enable", "true");
//        props.put("mail.smtp.ssl.socketFactory", mailSSLSocketFactory);

        //此处填写SMTP服务器
        props.put("mail.smtp.host", "smtp.qq.com");
        //端口号，QQ邮箱端口587
        props.put("mail.smtp.port", "465");
//        props.put("mail.smtp.port", "587");
        // 此处填写，写信人的账号
        props.put("mail.user", "3045785105@qq.com");
        // 此处填写16位STMP口令
        props.put("mail.password", "mrzugzpusnxudcdh");
        // 构建授权信息，用于进行SMTP进行身份验证
        Authenticator authenticator = new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                // 用户名、密码
                String userName = props.getProperty("mail.user");
                String password = props.getProperty("mail.password");
                return new PasswordAuthentication(userName, password);
            }
        };
        // 使用环境属性和授权信息，创建邮件会话
        Session mailSession = Session.getInstance(props, authenticator);
        // 创建邮件消息
        MimeMessage message = new MimeMessage(mailSession);
        // 设置发件人
        InternetAddress form = new InternetAddress(props.getProperty("mail.user"));
        message.setFrom(form);
        // 设置收件人的邮箱
        InternetAddress to = new InternetAddress(email);
        message.setRecipient(RecipientType.TO, to);
        // 设置邮件标题
        message.setSubject("邮件验证码测试");
        // 设置邮件的内容体
        message.setContent("尊敬的用户:你好!\n注册验证码为:" + code + "(有效期为一分钟,请勿告知他人)", "text/html;charset=UTF-8");
        // 最后当然就是发送邮件啦
        Transport.send(message);
    }

    public static String achieveCode() {  //由于数字 1 、 0 和字母 O 、l 有时分不清楚，所以，没有数字 1 、 0
        String[] beforeShuffle = new String[]{"2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F",
                "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "a",
                "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v",
                "w", "x", "y", "z"};
        List<String> list = Arrays.asList(beforeShuffle);//将数组转换为集合
        Collections.shuffle(list);  //打乱集合顺序
        StringBuilder sb = new StringBuilder();
        for (String s : list) {
            sb.append(s); //将集合转化为字符串
        }
        return sb.substring(3, 8);
    }
}

