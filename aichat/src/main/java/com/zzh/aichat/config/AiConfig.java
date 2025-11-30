package com.zzh.aichat.config;

import com.volcengine.ark.runtime.service.ArkService;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class AiConfig {

    @Value("${ai.apiKey}")
    private String apiKey;

    @Bean
    public ArkService arkService() {
        System.out.println("=== 初始化 ArkService ===");
        System.out.println("API Key: " + (apiKey != null ? "已配置" : "未配置"));

        ConnectionPool connectionPool = new ConnectionPool(5, 1, TimeUnit.SECONDS);
        Dispatcher dispatcher = new Dispatcher();

        return ArkService.builder()
                .dispatcher(dispatcher)
                .connectionPool(connectionPool)
                .baseUrl("https://ark.cn-beijing.volces.com/api/v3/")
                .apiKey(apiKey)
                .build();
    }
}