package com.carmaintenance.config;

import com.carmaintenance.security.JwtAuthenticationEntryPoint;
import com.carmaintenance.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Spring Security配置类
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors().and().csrf().disable()
                .exceptionHandling().authenticationEntryPoint(jwtAuthenticationEntryPoint).and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .authorizeRequests()
                // 公开访问的端点
                .antMatchers("/auth/**").permitAll()
                .antMatchers("/public/**").permitAll()
                // 门店公开端点（未登录用户可访问）
                .antMatchers("/shop/register").permitAll()
                .antMatchers("/shop/list").permitAll()
                .antMatchers("/shop/city/**").permitAll()
                .antMatchers("/shop/search").permitAll()
                .antMatchers(HttpMethod.GET, "/shop/*").permitAll()
                // 门店认证端点
                .antMatchers("/shop/auth/login").permitAll()
                // 文件上传端点（需要认证）
                .antMatchers("/upload/**").authenticated()
                // 服务项目和套餐公开端点（GET请求）
                .antMatchers(HttpMethod.GET, "/service-item/**").permitAll()
                .antMatchers(HttpMethod.GET, "/service-package/**").permitAll()
                // 公告公开端点
                .antMatchers("/announcement/active").permitAll()
                .antMatchers(HttpMethod.GET, "/announcement/**").permitAll()
                // 评价公开端点
                .antMatchers(HttpMethod.GET, "/review/stats/**").permitAll()
                .antMatchers(HttpMethod.GET, "/review/latest/**").permitAll()
                .antMatchers(HttpMethod.GET, "/review/shop/**").permitAll()
                // 会员等级公开端点
                .antMatchers(HttpMethod.GET, "/member-level/**").permitAll()
                // 门店管理端点（需要SHOP或ADMIN角色）
                .antMatchers("/shop/management/**").hasAnyRole("SHOP", "ADMIN")
                .antMatchers("/shop/auth/**").permitAll()
                // 管理员端点
                .antMatchers("/admin/**").hasRole("ADMIN")
                // 客户端点
                .antMatchers("/customer/**").hasAnyRole("CUSTOMER", "ADMIN")
                // 其他所有请求需要认证
                .anyRequest().authenticated();

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
