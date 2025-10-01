package com.gal.galend.web;

import com.gal.galend.domain.EmailOtpType;
import com.gal.galend.domain.User;
import com.gal.galend.repo.UserRepo;
import com.gal.galend.service.EmailOtpService;
import com.gal.galend.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.Map;


@RestController
@RequestMapping("/auth")
public class AuthController {

    private final EmailOtpService otp;
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepo users;
    private final BCryptPasswordEncoder encoder;
    @org.springframework.beans.factory.annotation.Autowired
    public AuthController(EmailOtpService otp, AuthService authService, PasswordEncoder passwordEncoder, UserRepo users, BCryptPasswordEncoder encoder) {
        this.otp = otp;
        this.authService = authService;
        this.passwordEncoder = passwordEncoder;
        this.users = users;
        this.encoder = encoder;
    }

    /* ---------- 工具 ---------- */

    /** 兼容 JSON body 或 form/query 取 email */
    private String extractEmail(HttpServletRequest req, Map<String, Object> body) {
        String email = null;
        if (body != null && body.get("email") != null) {
            email = String.valueOf(body.get("email")).trim();
        }
        if (email == null || email.isBlank()) {
            email = req.getParameter("email");
            if (email != null) email = email.trim();
        }
        return email;
    }

    /** 字符串转枚举，异常与空值时默认 VERIFY */
    private EmailOtpType parseType(String raw) {
        if (raw == null || raw.isBlank()) return EmailOtpType.VERIFY;
        try {
            return EmailOtpType.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return EmailOtpType.VERIFY;
        }
    }

    /* ---------- 验证码 ---------- */

    /**
     * 发送邮箱验证码
     * POST /auth/request-code?type=VERIFY|RESET
     * body: { "email": "xx@xx.com" } 或 form/query 传 email
     */
    @PostMapping("/request-code")
    public ResponseEntity<?> requestCode(
            @RequestParam(value = "type", required = false, defaultValue = "VERIFY") String typeParam,
            HttpServletRequest req,
            @RequestBody(required = false) Map<String, Object> body) {

        try {
            String email = null;
            if (body != null && body.get("email") != null) {
                email = String.valueOf(body.get("email")).trim();
            }
            if ((email == null || email.isBlank()) && req.getParameter("email") != null) {
                email = req.getParameter("email").trim();
            }
            if (email == null || email.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "缺少邮箱"));
            }

            EmailOtpType type = "RESET".equalsIgnoreCase(typeParam)
                    ? EmailOtpType.RESET : EmailOtpType.VERIFY;

            otp.sendCode(email, type);
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (RuntimeException e) {
            // 业务错误 → 400，避免 500
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 提交并校验验证码
     * POST /auth/verify-by-code?code=123456[&type=VERIFY|RESET]
     * body: { "email": "xx@xx.com" } 或 form/query 传 email
     */
    @PostMapping("/verify-by-code")
    public ResponseEntity<?> verifyByCode(
            @RequestParam String code,
            @RequestParam(defaultValue = "VERIFY") String type,
            @RequestBody(required = false) Map<String,Object> body,
            HttpServletRequest req) {

        // 兼容 body/form 取 email
        String email = body != null && body.get("email") != null
                ? String.valueOf(body.get("email")).trim().toLowerCase()
                : req.getParameter("email");

        if (email == null || email.isBlank() || code == null || code.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error","invalid params"));
        }

        EmailOtpType t = EmailOtpType.valueOf(type.toUpperCase());
        boolean ok = otp.verifyCode(t, email, code.trim()); // ← 顺序：(type,email,code)

        return ok ? ResponseEntity.ok().build()
                : ResponseEntity.status(400).body(Map.of("error","invalid code"));
    }


    /* ---------- 账号 ---------- */

    /**
     * 注册：前端已做“邮箱验证码成功”这一前置
     * body: { "displayName": "...", "email": "...", "password": "..." }
     */

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, Object> body,
                                      HttpServletResponse res) {
        final String name  = String.valueOf(body.getOrDefault("displayName", "")).trim();
        final String email = String.valueOf(body.getOrDefault("email", "")).trim().toLowerCase();
        final String pwd   = String.valueOf(body.getOrDefault("password", ""));

        if (name.isEmpty() || email.isEmpty() || pwd.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "missing fields"));
        }

        // 1) 取用户
        var u = users.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("请先验证邮箱"));

        // 2) 必须已通过邮箱验证
        if (!Boolean.TRUE.equals(u.isEmailVerified())) {
            return ResponseEntity.badRequest().body(Map.of("error", "请先完成邮箱验证"));
        }

        // 3) 设置（或覆盖）资料，完成注册
        // 如果你的 User 没有 displayName 字段，这行就删掉
        // u.setDisplayName(name);

        u.setPasswordHash(passwordEncoder.encode(pwd));
        users.save(u);

        // 4) 下发登录态（沿用你现有 login 或 jwt/cookie 颁发逻辑）
        authService.login(email, pwd, res);

        return ResponseEntity.ok(Map.of("ok", true));
    }


    /**
     * 登录
     * body: { "email": "...", "password": "..." }
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, Object> body,
                                   HttpServletResponse res) {
        String email = body.getOrDefault("email", "").toString().trim().toLowerCase();
        String pwd = body.getOrDefault("password", "").toString();

        if (email.isEmpty() || pwd.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "missing fields"));
        }

        authService.login(email, pwd, res);  // 内部设置 access/refresh cookie
        return ResponseEntity.ok(Map.of("ok", true));
    }

    /** 退出登录：清 cookie */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse res) {
        authService.logout(res);
        return ResponseEntity.ok().build();
    }

    // 用 6 位验证码重置密码
    @PostMapping("/reset-by-code")
    public ResponseEntity<?> resetByCode(@RequestBody Map<String, Object> body) {
        final String email = String.valueOf(body.getOrDefault("email", "")).trim().toLowerCase();
        final String code  = String.valueOf(body.getOrDefault("code",  "")).trim();
        final String npwd  = String.valueOf(body.getOrDefault("newPassword", ""));

        if (email.isEmpty() || code.isEmpty() || npwd.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "missing fields"));
        }

        try {
            authService.resetByOtp(email, code, npwd);  // 调用你已实现的服务
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

}