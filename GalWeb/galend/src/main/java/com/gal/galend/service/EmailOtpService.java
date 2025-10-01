package com.gal.galend.service;

import com.gal.galend.domain.EmailOtp;
import com.gal.galend.domain.EmailOtpType;
import com.gal.galend.domain.User;
import com.gal.galend.repo.EmailOtpRepo;
import com.gal.galend.repo.UserRepo;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service

public class EmailOtpService {
    private static final Logger log = LoggerFactory.getLogger(EmailOtpService.class);
    private final UserRepo users;
    private final EmailOtpRepo repo;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}") String from;

    public EmailOtpService(UserRepo users, EmailOtpRepo repo, JavaMailSender mailSender) {
        this.users = users;
        this.repo = repo;
        this.mailSender = mailSender;
    }
    /** 你原来就有的设置，随你的项目而定 */
    private final int maxAttempts = 5;
    private final int ttlMinutes  = 10;

    /**
     * 发送验证码：
     * - 若用户不存在且 type==VERIFY，则创建一条“未验证”的临时用户后再发码；
     * - 其它类型（如 RESET）用户必须已存在，否则抛错。
     */
    @Transactional
    public void sendCode(String email, EmailOtpType type) {

        email = email.trim().toLowerCase(Locale.ROOT);

        // 0) 取/建用户
        User user = users.findByEmailIgnoreCase(email).orElse(null);
        if (user == null) {
            if (type != EmailOtpType.VERIFY) {
                throw new RuntimeException("用户不存在");
            }
            user = new User();
            user.setEmail(email);
            user.setEmailVerified(false);
            // 若实体要求可补：user.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            users.save(user);
        }

        // 可选：失效旧验证码（若你的 repo 有这个方法就打开）
        repo.invalidateActive(user.getId(), type, LocalDateTime.now());
        // 1) 生成并保存 OTP（省略：你现有的 findActive / invalidate / 保存逻辑）

        String code = gen6(); // 例如生成 6 位
        EmailOtp otp = new EmailOtp();
        otp.setUserId(user.getId());
        otp.setType(type);
        otp.setCode(code);
        otp.setSentAt(LocalDateTime.now());
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(ttlMinutes));
        otp.setAttempts(0);
        repo.save(otp);

        // 2) 发送邮件
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper h = new MimeMessageHelper(msg, "UTF-8");
            h.setFrom(from);                    // 必须与 spring.mail.username 完全一致，不要带昵称
            h.setTo(email);
            h.setSubject("验证码");
            h.setText("您的验证码是 " + code + "，" + ttlMinutes + " 分钟内有效。", false);
            mailSender.send(msg);
        } catch (Exception e) {
            // 打日志并抛出，避免前端以为成功
            log.error("发送验证码邮件失败: {}", e.getMessage(), e);
            throw new RuntimeException("邮件发送失败");
        }
    }

    /**
     * 校验验证码：
     * - 命中：标记 OTP 已用；若 type==VERIFY，给用户打上 emailVerified=true
     * - 不命中：attempts+1
     */
    @Transactional
    public boolean verifyCode(EmailOtpType type, String email, String code) {
        if (email == null || email.isBlank() || code == null || code.isBlank()) {
            throw new RuntimeException("参数无效");
        }
        email = email.trim().toLowerCase(Locale.ROOT);
        code  = code.trim();

        // 1) 找用户
        User user = users.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 2) 找到当前有效的 OTP（你仓库已有这个查询方法）
        LocalDateTime now = LocalDateTime.now();
        var actives = repo.findActive(user.getId(), type, now);
        if (actives.isEmpty()) {
            throw new RuntimeException("验证码无效或已过期");
        }
        EmailOtp otp = actives.get(0);

// 3) 次数保护
        int attempts = otp.getAttempts();           // 基本类型 int
        if (attempts >= maxAttempts) {
            throw new RuntimeException("尝试次数过多，请重新获取验证码");
        }

// 4) 比对
        if (!code.equals(otp.getCode())) {
            otp.setAttempts(attempts + 1);          // 直接 +1
            repo.save(otp);
            throw new RuntimeException("验证码不正确");
        }


        // 5) 命中：标记已用
        repo.markUsed(otp.getId(), now);

        // 6) VERIFY：把用户标记为已验证
        if (type == EmailOtpType.VERIFY && !Boolean.TRUE.equals(user.isEmailVerified())) {
            user.setEmailVerified(true);       // 注意：是 isEmailVerified()/setEmailVerified(boolean)
            users.save(user);
        }
        return true;
    }

    private String gen6() {
        int x = (int)(Math.random() * 900000) + 100000;
        return String.valueOf(x);
    }
}
