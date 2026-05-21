package com.xushu.rag.controller;

import com.xushu.rag.advisors.MetadataAwareQuestionAnswerAdvisor;
import com.xushu.rag.annotation.Loggable;
import com.xushu.rag.common.ApplicationConstant;
import com.xushu.rag.common.KnowledgeEnum;
import com.xushu.rag.context.BaseContext;
import com.xushu.rag.entity.SensitiveWord;
import com.xushu.rag.service.SensitiveWordService;
import com.xushu.rag.utils.ChatClientFactory;
import com.xushu.rag.utils.RedissonUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;


/**
 * @Title: ChatController
 * @Author Xushu
 * @Package com.Xushu.rag.controller
 * @description: 对话接口
 */

@Tag(name="AiRagController",description = "chat对话接口")
@Slf4j
@RestController
@RequestMapping(ApplicationConstant.API_VERSION + "/chat")
public class ChatController {



    @Autowired
    private SensitiveWordService sensitiveWordService;




    @Autowired
    private RedissonUtil redissonUtil;




    @Operation(summary = "stream",description = "流式对话接口")
    @GetMapping(value = "/stream",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Loggable("message")
    public Flux<String> streamRagChat(@RequestParam(value = "message", defaultValue = "你好" ) String message,
                                      @RequestParam(value = "prompt", defaultValue = "你是一名AI助手，致力于帮助人们解决问题.") String prompt){
        List<SensitiveWord> list = sensitiveWordService.list();

        for(SensitiveWord sensitiveWord: list){
            if (message.contains(sensitiveWord.getWord())){
                return Flux.just("包含敏感词:" + sensitiveWord.getWord());
            }
        }

        Long userId = BaseContext.getCurrentId();
        ChatClient chatClient = ChatClientFactory.getChatClient();
        Flux<String> content = chatClient.prompt()
                .system(prompt)
                .advisors(new MetadataAwareQuestionAnswerAdvisor())
                .advisors(a -> {
                    a.param(ChatMemory.CONVERSATION_ID, userId);
                })
                .user(message)
                .stream()
                .content();
        return content;
    }






}
