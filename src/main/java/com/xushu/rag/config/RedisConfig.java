package com.xushu.rag.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.redisson.spring.data.connection.RedissonConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Value("${spring.data.redis.password:}")
    private String redisPassword;

    @Value("${spring.data.redis.database:0}")
    private int redisDatabase;

    /**
     * 配置 Redisson 客户端，使用 JSON 序列化器
     */
    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();

        // 使用 JSON 序列化器，避免乱码问题
        config.setCodec(new JsonJacksonCodec());

        String address = "redis://" + redisHost + ":" + redisPort;

        config.useSingleServer()
                .setAddress(address)
                .setDatabase(redisDatabase)
                .setPassword(redisPassword.isEmpty() ? null : redisPassword);

        return Redisson.create(config);
    }

//    /**
//     * 配置 Redisson 连接工厂
//     */
//    @Bean
//    public RedissonConnectionFactory redissonConnectionFactory(RedissonClient redissonClient) {
//        return new RedissonConnectionFactory(redissonClient);
//    }

//    @Bean
//    public RedisTemplate<String, Object> redisTemplate(org.springframework.data.redis.connection.RedisConnectionFactory factory) {
//        RedisTemplate<String, Object> template = new RedisTemplate<>();
//        template.setConnectionFactory(factory);
//        template.setKeySerializer(new StringRedisSerializer());
//        template.setValueSerializer(new StringRedisSerializer());
//        template.setHashKeySerializer(new StringRedisSerializer());
//        template.setHashValueSerializer(new StringRedisSerializer());
//        template.afterPropertiesSet();
//        return template;
//    }
}
