package com.yupi.yupao.config;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redisson 配置
  
 */
@Configuration
@ConfigurationProperties(prefix = "spring.redis")
@Data
public class RedissonConfig {

    private String host;

    private String port;

    // 你在 yml 里配置的 password 也要读取进来！
    private String password;

    @Bean
    public RedissonClient redissonClient() {
        // 1. 创建配置
        Config config = new Config();
        String redisAddress = String.format("redis://%s:%s", host, port);

        // 在这里添加密码 + 使用你配置的 database
        config.useSingleServer()
                .setAddress(redisAddress)
                .setPassword(password)  // 👈 关键：加密码
                .setDatabase(1);       // 👈 关键：和你 yml 里的 database 保持一致（你写的是1）

        // 2. 创建实例
        RedissonClient redisson = Redisson.create(config);
        return redisson;
    }
}