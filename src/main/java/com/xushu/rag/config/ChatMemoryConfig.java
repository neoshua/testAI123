package com.xushu.rag.config;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.memory.redis.RedissonRedisChatMemoryRepository;
import org.redisson.api.RedissonClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
//import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;


@Configuration
public class ChatMemoryConfig {

    @Resource
    private DashScopeChatModel dashscopeChatModel;

//    @Resource
//    private OllamaChatModel ollamaChatModel;

    @Resource
    private RedissonClient redissonClient;



    @Bean
    public ChatMemoryRepository inMemoryChatMemoryRepository() {
        return new InMemoryChatMemoryRepository();
    }



    @Bean
    public RedissonRedisChatMemoryRepository redisChatMemoryRepository() {
        RedissonRedisChatMemoryRepository.RedissonBuilder builder =
                RedissonRedisChatMemoryRepository.builder()
                        .redissonConfig(redissonClient.getConfig());
        return builder.build();
    }




    //构建redis 记忆

    @Bean
    public ChatMemory chatMemory(ChatMemoryRepository redisChatMemoryRepository) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(redisChatMemoryRepository)
                .maxMessages(10)
                .build();
    }
}
