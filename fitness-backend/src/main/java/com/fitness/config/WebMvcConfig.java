package com.fitness.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC配置 - 静态资源访问
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置上传文件的访问路径
        // 应用运行在 fitness-backend 目录下，uploads 文件夹也在这里
        String uploadPath = System.getProperty("user.dir") + "/uploads/";
        
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath);
        
        System.out.println("========================================");
        System.out.println("静态资源配置: /api/uploads/** -> file:" + uploadPath);
        System.out.println("工作目录: " + System.getProperty("user.dir"));
        System.out.println("========================================");
    }
}
