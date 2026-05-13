package com.wonwire.wonwire.config;

import redis.embedded.RedisServer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class EmbeddedRedisConfig {

    @Bean(destroyMethod = "stop")
    public RedisServer redisServer() throws Exception {
        RedisServer server = new RedisServer(6379);
        server.start();
        return server;
    }
}