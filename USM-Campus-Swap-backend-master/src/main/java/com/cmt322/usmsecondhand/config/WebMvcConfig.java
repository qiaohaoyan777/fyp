package com.cmt322.usmsecondhand.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 🚀 终极一击：利用 Java 官方 URI 库自动生成标准 file:/// 路径，跨平台绝不出错
        File dir = new File(uploadDir);
        String p = dir.toURI().toString(); 

        // 让 /images/** 和 /uploads/** 完美映射
        registry.addResourceHandler("/images/**", "/uploads/**")
                .addResourceLocations(p);
        
        System.out.println("====================================================");
        System.out.println("🚀 [Campus Swap 静态资源服务器全面接管]");
        System.out.println("🔊 电脑中真实的图片文件夹位置: " + dir.getAbsolutePath());
        System.out.println("🔊 强行喂给 Spring 的标准 URI: " + p);
        System.out.println("====================================================");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowCredentials(true)
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("*");
    }
}   