package com.yupi.yupao.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket 配置
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 启用简单的内存消息代理，前缀为 /topic（广播）和 /queue（点对点）
        config.enableSimpleBroker("/topic", "/queue");
        // 设置应用前缀，客户端发送消息的前缀
        config.setApplicationDestinationPrefixes("/app");
        // 设置用户前缀，用于点对点消息
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 注册 WebSocket 端点，客户端连接地址
        registry.addEndpoint("/ws/chat")
                .setAllowedOrigins("http://localhost:3000") // 允许的前端地址
                .withSockJS(); // 启用 SockJS 回退选项
    }
}
