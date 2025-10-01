package com.gal.galend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

//@Configuration
//public class SecurityConfig {
//    private final JwtCookieFilter jwtFilter;
//    public SecurityConfig(JwtCookieFilter f){ this.jwtFilter = f; }
//
//    @Bean BCryptPasswordEncoder passwordEncoder(){ return new BCryptPasswordEncoder(); }
//
//    @Bean
//    SecurityFilterChain chain(HttpSecurity http) throws Exception {
//        http.csrf(csrf -> csrf.disable());
//        // 显式使用我们下方的 corsConfigurationSource()
//        http.cors(Customizer.withDefaults());
//        http.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
//        http.authorizeHttpRequests(reg -> reg
//                // 放开所有预检
//                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
//                .requestMatchers(HttpMethod.GET, "/ping").permitAll()
//                // 放开认证接口和 ping
//                .requestMatchers("/auth/**").permitAll()
//                .anyRequest().authenticated()
//        );
//        // 方便定位 401/403
//        http.exceptionHandling(e -> e
//                .authenticationEntryPoint((req,res,ex)->res.sendError(401))
//                .accessDeniedHandler((req,res,ex)->res.sendError(403))
//        );
//        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
//        return http.build();
//    }
//
//
//    @Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration cfg = new CorsConfiguration();
//        // 你的页面是 127.0.0.1:5500，精确写它；如果你有时用 localhost，也可都写：
//        cfg.setAllowedOrigins(List.of("http://127.0.0.1:5500", "http://localhost:5500"));
//        cfg.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
//        cfg.setAllowedHeaders(List.of("*"));
//        cfg.setAllowCredentials(true);
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", cfg);
//        return source;
//    }
//
//}

@Configuration
public class SecurityConfig {
    private final JwtCookieFilter jwtFilter;
    public SecurityConfig(JwtCookieFilter f){ this.jwtFilter = f; }

    @Bean BCryptPasswordEncoder passwordEncoder(){ return new BCryptPasswordEncoder(); }

    @Bean
    SecurityFilterChain chain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable());
        http.cors(Customizer.withDefaults());
        http.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // —— 先全面放行做排错 —— //
        http.authorizeHttpRequests(reg -> reg
                .anyRequest().permitAll()
        );

        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(List.of("https://galweb.pages.dev","http://127.0.0.1:5500","http://localhost:5500"));
        cfg.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}



