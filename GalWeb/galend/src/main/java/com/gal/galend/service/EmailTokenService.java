package com.gal.galend.service;

import com.gal.galend.domain.EmailToken;
import com.gal.galend.domain.EmailTokenType;
import com.gal.galend.repo.EmailTokenRepo;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class EmailTokenService {
    private final EmailTokenRepo repo;
    private final JavaMailSender mail;
    private final String front; // 只保留一处注入：构造器注入

    private static final Logger log = LoggerFactory.getLogger(EmailTokenService.class);

    public EmailTokenService(EmailTokenRepo repo,
                             JavaMailSender mail,
                             @Value("${app.frontend.origin}") String front) {
        this.repo = repo;
        this.mail = mail;
        this.front = front;
    }

    public EmailToken create(String userId, EmailTokenType type, int minutes){
        String token = UUID.randomUUID().toString().replace("-", "")
                + UUID.randomUUID().toString().replace("-", "");
        EmailToken et = new EmailToken();
        et.setUserId(userId);
        et.setToken(token);
        et.setType(type);
        et.setExpiresAt(java.time.LocalDateTime.now().plusMinutes(minutes));
        // used_at 留空（null）
        return repo.save(et);
    }

    public void sendVerify(String email, String token){
        String url = front + "/#/verify?token=" + token;
        send(email, "Verify your email", "Click to verify: " + url, url);
    }

    public void sendReset(String email, String token){
        String url = front + "/#/reset?token=" + token;
        send(email, "Reset your password", "Click to reset: " + url, url);
    }

    // 第四个参数 url 仅用于日志打印
    private void send(String to, String subject, String text, String urlForLog){

        try{
            // 纯文本足够；若以后要 HTML 再换 MimeMessageHelper
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom("2911925491@qq.com");
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(text);
            mail.send(msg);
            log.info("[MAIL] {} -> {} 已发送", subject, to);
        }catch(Exception e){
            // 不要影响业务流程：仅警告并继续
            log.warn("[MAIL] 发送失败，进入开发兜底。to={} subject={}", to, subject, e);
        }finally{
            // 无论成功失败，都打印一条可点击的链接
            if (urlForLog != null) {
                log.info("[DEV] 邮件链接：{}", urlForLog);
            }
        }
    }
}

