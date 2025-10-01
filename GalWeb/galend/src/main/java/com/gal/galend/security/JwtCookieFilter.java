package com.gal.galend.security;

//import com.gal.galend.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

//@Component
//public class JwtCookieFilter extends OncePerRequestFilter {
//    private final JwtService jwt;
//    public JwtCookieFilter(JwtService jwt){ this.jwt = jwt; }
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
//            throws ServletException, IOException {
//        System.out.println("[jwt-filter] hit path=" + req.getRequestURI());  // 临时排错点
//
//        try {
//            final String uri = req.getRequestURI();
//            // 1) 跳过无需鉴权的请求（预检/公开路由/静态）
//            if ("OPTIONS".equalsIgnoreCase(req.getMethod())
//                    || uri.startsWith("/auth/")
//                    || "/ping".equals(uri)
//                    || uri.startsWith("/assets/") || uri.startsWith("/static/")) {
//                chain.doFilter(req, res);
//                return;
//            }
//
//            // 2) 已有认证就不重复设置
//            if (SecurityContextHolder.getContext().getAuthentication() == null) {
//                Cookie[] cs = req.getCookies();
//                if (cs != null) {
//                    String access = Arrays.stream(cs)
//                            .filter(c -> "access".equals(c.getName()))
//                            .findFirst()
//                            .map(Cookie::getValue)
//                            .orElse(null);
//
//                    if (access != null && !access.isBlank()) {
//                        // 3) 验证并解析 userId（你已有的方法）
//                        String uid = jwt.parseSubject(access); // == userId
//                        if (uid != null && !uid.isBlank()) {
//                            var auth = new UsernamePasswordAuthenticationToken(uid, null, java.util.List.of());
//                            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
//                            SecurityContextHolder.getContext().setAuthentication(auth);
//                        }
//                    }
//                }
//            }
//        } catch (Exception ignored) {
//            // token 无效/过期等直接忽略，按未登录继续走
//        }
//
//        chain.doFilter(req, res);
//    }
//}
@Component
public class JwtCookieFilter extends OncePerRequestFilter {
    private final JwtService jwt;
    public JwtCookieFilter(JwtService jwt){ this.jwt = jwt; }

    private static final String[] SKIP_PREFIX = {"/auth/","/assets/","/static/"};
    private static final String[] SKIP_EXACT  = {"/ping","/favicon.ico","/error"};

    private boolean shouldSkip(HttpServletRequest req){
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) return true;
        String p = req.getServletPath();
        for (String s: SKIP_EXACT)  if (p.equals(s))     return true;
        for (String s: SKIP_PREFIX) if (p.startsWith(s)) return true;
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        if (!shouldSkip(req) && SecurityContextHolder.getContext().getAuthentication()==null) {
            String token = null;
            Cookie[] cs = req.getCookies();
            if (cs != null) {
                for (Cookie c : cs) { if ("access".equals(c.getName())) { token = c.getValue(); break; } }
            }
            if (token != null && !token.isBlank()) {
                try {
                    String uid = jwt.parseSubject(token);
                    if (uid != null && !uid.isBlank()) {
                        var auth = new UsernamePasswordAuthenticationToken(uid, null, Collections.emptyList());
                        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                } catch (Exception ignored) {
                    SecurityContextHolder.clearContext();
                }
            }
        }
        chain.doFilter(req, res);
    }
}