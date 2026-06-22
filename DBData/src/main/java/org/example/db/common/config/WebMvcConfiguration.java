package org.example.db.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;


@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {
    @Bean
    public AuthenticeInterceptor getAuthenticeInterceptor() {
        return new AuthenticeInterceptor();
    }
    
    /**
     * 自定义拦截器，添加拦截路径和排除拦截路径
     * addPathPatterns(): 添加需要拦截的路径
     * excludePathPatterns(): 添加不需要拦截的路径
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
    
        List<String> excludePathList = new ArrayList<>();
    
        excludePathList.add("/login/**");
        excludePathList.add("/doLogin");
        excludePathList.add("/doRegister");
    
        excludePathList.add("/css/**");
        excludePathList.add("/js/**");
        excludePathList.add("/images/**");
        excludePathList.add("/static/**");
        excludePathList.add("/favicon.ico");
    
        registry.addInterceptor(getAuthenticeInterceptor())
                .addPathPatterns("/api/**")   // ✔ 只拦 API
                .excludePathPatterns(excludePathList);
    }
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        
        registry.addViewController("/{path:[^\\.]*}")
                .setViewName("forward:/index.html");
        
        registry.addViewController("/**/{path:[^\\.]*}")
                .setViewName("forward:/index.html");
    }
    
    /**
     * 配置静态资源映射
     *
     * @param registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置静态资源不被拦截
        registry.addResourceHandler("/**").addResourceLocations("classpath:/static/");
        // 配置swagger-ui不被拦截(knife4j)
        registry.addResourceHandler("swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("doc.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
    }
    
}


