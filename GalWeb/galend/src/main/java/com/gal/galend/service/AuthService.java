package com.gal.galend.service;

import com.gal.galend.domain.*;
import com.gal.galend.repo.*;
import com.gal.galend.security.CookieUtil;
import com.gal.galend.security.JwtService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.UUID;
import com.gal.galend.domain.User;
import com.gal.galend.domain.Profile;
import com.gal.galend.domain.EmailToken;
import com.gal.galend.domain.EmailTokenType;
@Service
public class AuthService {
    private final UserRepo users;
    private final ProfileRepo profiles;
    private final EmailTokenRepo emailTokens;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final CookieUtil cookie;
    private final EmailTokenService emailSvc;

    // 注入
    private final EmailOtpService otpSvc;

    public AuthService(UserRepo users,
                       ProfileRepo profiles,
                       EmailTokenRepo emailTokens,
                       PasswordEncoder encoder,
                       JwtService jwt,
                       CookieUtil cookie,
                       EmailTokenService emailSvc,
                       EmailOtpService otpSvc) {
        this.users = users;
        this.profiles = profiles;
        this.emailTokens = emailTokens;
        this.encoder = encoder;
        this.jwt = jwt;
        this.cookie = cookie;
        this.emailSvc = emailSvc;
        this.otpSvc = otpSvc;
    }




    public void register(String email, String rawPassword, String displayName){
        email = email == null ? null : email.trim();
        rawPassword = rawPassword == null ? null : rawPassword.trim();
        // 1) 重复邮箱校验
        users.findByEmailIgnoreCase(email)
                .ifPresent(u -> { throw new RuntimeException("email already used"); });

        // 2) 创建 User（普通实体 + setter）
        var user = new User();
        user.setId(UUID.randomUUID().toString());         // 也可依赖 @PrePersist 自动生成
        user.setEmail(email.trim());
        user.setPasswordHash(encoder.encode(rawPassword));
        user.setEmailVerified(false);
        user = users.save(user);                          // 持久化后可安全取 getId()

        // 3) 创建 Profile（同样用 setter）
        var p = new Profile();
        p.setUserId(user.getId());
        p.setDisplayName(displayName == null ? "User" : displayName.trim());
        p.setBio("");
        p.setAvatarUrl("");
        profiles.save(p);

        // 4) 发验证邮件（30 分钟有效）
        int minutes = 30; // 你也可以 @Value("${app.mail.verifyMinutes:30}") 注入
        EmailToken tok = emailSvc.create(user.getId(), EmailTokenType.VERIFY, minutes);
        emailSvc.sendVerify(email, tok.getToken());
    }
    public void login(String email, String rawPassword, HttpServletResponse resp){
        email = email == null ? null : email.trim();
        rawPassword = rawPassword == null ? null : rawPassword.trim();
        var user = users.findByEmailIgnoreCase(email).orElseThrow(() -> new RuntimeException("账号或密码错误"));
        if(!encoder.matches(rawPassword, user.getPasswordHash())) throw new RuntimeException("账号或密码错误");
        var token = jwt.issueAccess(user.getId());
        cookie.write(resp, "access", token, 60 * 15);
    }

    public void logout(HttpServletResponse resp){ cookie.clear(resp); }

    public void requestVerify(String email){
        var user = users.findByEmailIgnoreCase(email).orElseThrow(() -> new RuntimeException("用户不存在"));
        var tok = emailSvc.create(user.getId(), EmailTokenType.VERIFY, 60*24);
        emailSvc.sendVerify(email, tok.getToken());
    }

    public void verify(String token){
        var et = emailTokens.findByToken(token).orElseThrow(() -> new RuntimeException("链接无效"));
        if(et.getUsedAt()!=null || et.getExpiresAt().isBefore(LocalDateTime.now())) throw new RuntimeException("链接过期");
        var user = users.findById(et.getUserId()).orElseThrow();
        user.setEmailVerified(true); users.save(user);
        et.setUsedAt(LocalDateTime.now()); emailTokens.save(et);
    }

    public void requestReset(String email){
        var user = users.findByEmailIgnoreCase(email).orElseThrow(() -> new RuntimeException("用户不存在"));
        var tok = emailSvc.create(user.getId(), EmailTokenType.RESET, 30);
        emailSvc.sendReset(email, tok.getToken());
    }

    public void resetPassword(String token, String newPassword){
        var et = emailTokens.findByToken(token).orElseThrow(() -> new RuntimeException("链接无效"));
        if(et.getUsedAt()!=null || et.getExpiresAt().isBefore(LocalDateTime.now())) throw new RuntimeException("链接过期");
        var user = users.findById(et.getUserId()).orElseThrow();
        user.setPasswordHash(encoder.encode(newPassword));
        users.save(user);
        et.setUsedAt(LocalDateTime.now()); emailTokens.save(et);
    }


    // 发送验证码（忘记密码）
    public void requestResetCode(String email){
        otpSvc.sendCode(email, EmailOtpType.RESET);
    }

    // 发送验证码（验证邮箱）
    public void requestVerifyCode(String email){
        otpSvc.sendCode(email, EmailOtpType.VERIFY);
    }

    // 使用验证码验证邮箱
    public void verifyByCode(String email, String code){
        otpSvc.verifyCode(EmailOtpType.VERIFY, email, code);
    }

    // 使用验证码重置密码
    @Transactional
    public void resetByOtp(String email, String code, String newPassword){
        // 先校验 OTP（内部会做过期/次数）
        otpSvc.verifyCode(EmailOtpType.RESET, email, code);
        // 设置新密码
        var u = users.findByEmailIgnoreCase(email).orElseThrow(() -> new RuntimeException("用户不存在"));
        u.setPasswordHash(encoder.encode(newPassword));
        users.save(u);
    }




}
