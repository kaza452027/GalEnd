package com.gal.galend.security;

import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {
    @Value("${app.cookie.name:access}") private String cookieName;
    @Value("${app.cookie.secure:false}") private boolean secure;
    @Value("${app.cookie.sameSite:Lax}") private String sameSite;
    @Value("${app.cookie.domain:localhost}") private String domain;

    public void write(HttpServletResponse resp, String name, String value, int maxAgeSeconds) {
        Cookie c = new Cookie(name, value);
        c.setHttpOnly(true);
        c.setSecure(false);      // 本地是 http，不能 true
        c.setPath("/");
        c.setMaxAge(maxAgeSeconds);
        // 如果用 Servlet 6 想显式 SameSite，可自己拼 Set-Cookie 头；默认 Lax 即可
        resp.addCookie(c);
    }
    public void clear(HttpServletResponse resp) {
        for (String name : new String[]{"access", "refresh"}) {
            Cookie c = new Cookie(name, "");
            c.setPath("/");
            c.setHttpOnly(true);
            c.setSecure(false);
            c.setMaxAge(0);
            resp.addCookie(c);
        }
    }
}
