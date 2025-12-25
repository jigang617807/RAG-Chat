package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
/*
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${upload.dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        //   /uploads/** → D:/javalearning/demo/uploads/
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadDir + "/uploads/");
    }
}*/

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${upload.dir}")
    private String uploadDir; // 这里现在读取到的是 "."

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 1. 获取当前项目根目录的绝对路径
        // 如果 uploadDir 是 "."，这里会得到类似 "D:\javalearning\demo" 的路径
        String absolutePath = new File(uploadDir).getAbsolutePath();

        // 2. 确保路径以分隔符结尾 (兼容 Windows/Mac/Linux)
        if (!absolutePath.endsWith(File.separator)) {
            absolutePath += File.separator;
        }

        // 3. 映射路径
        // 最终效果：/uploads/** ->  file:D:/javalearning/demo/uploads/
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + absolutePath + "uploads/");
    }
}