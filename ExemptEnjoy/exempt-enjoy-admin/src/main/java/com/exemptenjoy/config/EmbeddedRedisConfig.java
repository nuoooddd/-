package com.exemptenjoy.config;

import java.io.IOException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.context.annotation.Configuration;
import redis.embedded.RedisServer;

/**
 * 本地嵌入式 Redis 启动配置，用于无 Redis 环境快速部署演示
 */
@Configuration
public class EmbeddedRedisConfig
{
    private RedisServer redisServer;

    @PostConstruct
    public void startRedis()
    {
        try
        {
            redisServer = new RedisServer(6379);
            redisServer.start();
        }
        catch (Exception e)
        {
            // 如果 6379 已被占用则忽略，允许用户自行启动 Redis
        }
    }

    @PreDestroy
    public void stopRedis() throws IOException
    {
        if (redisServer != null && redisServer.isActive())
        {
            redisServer.stop();
        }
    }
}
