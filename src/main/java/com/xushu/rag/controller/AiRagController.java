/*
 * Copyright 2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.xushu.rag.controller;

import com.alibaba.cloud.ai.advisor.RetrievalRerankAdvisor;
import com.alibaba.cloud.ai.dashscope.rerank.DashScopeRerankModel;
import com.alibaba.fastjson2.JSON;
import com.xushu.rag.advisors.MetadataAwareQuestionAnswerAdvisor;
import com.xushu.rag.annotation.Loggable;
import com.xushu.rag.common.ApplicationConstant;
import com.xushu.rag.config.AdaptiveManager;
import com.xushu.rag.constant.ChatModelEnum;
import com.xushu.rag.context.BaseContext;
import com.xushu.rag.entity.SensitiveWord;
import com.xushu.rag.service.SensitiveWordService;
import com.xushu.rag.utils.ChatClientFactory;
import io.jsonwebtoken.lang.Collections;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Tag(name = "AiRagController", description = "Rag接口")
@Slf4j
@RestController
@RequestMapping(ApplicationConstant.API_VERSION + "/ai")
public class AiRagController {
    @Resource
    private ChatModel dashscopeChatModel;

    @Autowired
    private DashScopeRerankModel dashScopeRerankModel;
    @Autowired
    private SensitiveWordService sensitiveWordService;
    @Autowired
    private AdaptiveManager adaptiveManager;

    @Autowired
    private VectorStore vectorStore;

    @Operation(summary = "rag", description = "Rag对话接口")
    @PostMapping(value = "/rag")
    @Loggable
    public Flux<String> generate(@RequestParam(value = "sources", required = false) List<String> sources,
                                 @RequestParam(value = "message", defaultValue = "你好") String message) throws IOException {

        // 敏感词过滤
        List<SensitiveWord> list = sensitiveWordService.list();
        for (SensitiveWord sensitiveWord : list) {
            if (message.contains(sensitiveWord.getWord())) {
                return Flux.just("包含敏感词:" + sensitiveWord.getWord());
            }
        }


        return processNormalRagQuery(sources,message);
    }

    private Flux<String> processNormalRagQuery(List<String> sources, String message) {
        Long userId = BaseContext.getCurrentId();

        // 使用ChatClient进行流式RAG对话 先构建普通的
        ChatClient chatClient = ChatClientFactory.getChatClient(ChatModelEnum.RAG);
        ChatClient.ChatClientRequestSpec chatClientRequestSpec = chatClient.prompt()
                .system(a -> {
                    a.param("current_data", LocalDate.now().toString());
                })
                .user(message)
                .advisors(new MetadataAwareQuestionAnswerAdvisor())
                .advisors(a -> {
                    a.param(ChatMemory.CONVERSATION_ID, userId);
                    a.param("userMessage", message);
                 });
        //如果有RAG，需要天啊及rag的拦截器
        if(!Collections.isEmpty(sources)) {

            //查询增强
//            RetrievalAugmentationAdvisor.builder()
//                    .documentRetriever(VectorStoreDocumentRetriever.builder()
//                            .similarityThreshold(0.1d)
//                            .vectorStore(vectorStore)
//                            .build())
//                    .queryAugmenter(ContextualQueryAugmenter.builder()
//                            .allowEmptyContext(false)
//                            .emptyContextPromptTemplate(PromptTemplate.builder().template("用户查询位于知识库之外。礼貌地告知用户您无法回答").build())
//                            .build())
//                    .queryTransformers(RewriteQueryTransformer.builder()
//                            .chatClientBuilder(ChatClient.builder(dashscopeChatModel))
//                            .build())
//                    .build();

//
//            RetrievalRerankAdvisor retrievalRerankAdvisor =
//                    new RetrievalRerankAdvisor(vectorStore,dashScopeRerankModel,
//                            SearchRequest.builder()
//                                    .topK(200).build());



            SearchRequest.Builder searchRequestBuilder = SearchRequest.builder()
//                    .query(message)
                    .similarityThreshold(0.1d).topK(5)
                    // source in ['xxx.pdf','xxxx']
                    .filterExpression("source in "+JSON.toJSONString(sources));

            chatClientRequestSpec
                    .system(a -> {
                        a.param("rag_message", """
                                如果涉及RAG，请提供文件来源，我会提供给你文件来源，
                                    请严格基于知识库内容回答用户问题，
                                    不要添加任何知识库之外的信息。如果知识库内容不完整，仅需基于已有信息作答，
                                    不要自行补充。
                                """);
                    })
                    .advisors(adaptiveManager.getQuestionAnswerAdvisors("source in "+JSON.toJSONString(sources)));

        }



        return chatClientRequestSpec.stream()
                .content();
    }
}