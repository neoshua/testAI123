package com.xushu.rag.config;

import com.alibaba.fastjson2.JSON;
import com.xushu.rag.common.DocumentEnum;
import com.xushu.rag.common.KnowledgeEnum;
import com.xushu.rag.context.BaseContext;
import com.xushu.rag.utils.ChatClientFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.VectorStoreChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Component
public class AdaptiveManager {


    @Resource
    private ChatMemory chatMemory;


    @Resource
    private VectorStore vectorStore;


    public List<Advisor>  getChatMemoryAdvisors() {

        List<Advisor> advisors = new ArrayList<>();
        advisors.add(PromptChatMemoryAdvisor.builder(chatMemory).order(0).build());
//        advisors.add(VectorStoreChatMemoryAdvisor.builder(vectorStore).defaultTopK(2).order(1).build());
        advisors.add(SimpleLoggerAdvisor.builder()
                .requestToString(JSON::toJSONString)
                .requestToString(JSON::toJSONString).build());
//
        return advisors;
    }


    public Advisor getQuestionAnswerAdvisors(String textExpression) {
//        var b = new FilterExpressionBuilder();
//
//        FilterExpressionBuilder.Op document = b.eq(KnowledgeEnum.TYPE.getType(), DocumentEnum.DOCUMENT.getName());
//        FilterExpressionBuilder.Op userId = b.eq(KnowledgeEnum.USER_ID.getType(), BaseContext.getCurrentId());
//        FilterExpressionBuilder.Op and = b.and(document, userId);


        SearchRequest.Builder searchRequestBuilder =
                SearchRequest.builder()
                        .similarityThreshold(0.1d)
                        .filterExpression(textExpression)
                        .topK(5);

        return QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(searchRequestBuilder.build())
                .build();


//        return QuestionAnswerAdvisor.builder(vectorStore)
//                .searchRequest(SearchRequest.builder()
//                        .topK(5)
//                        .filterExpression(textExpression)
//                        .similarityThreshold(0.1d)
//                        .build())
//                .build();
    }
    public Advisor getQuestionAnswerAdvisors() {
//        var b = new FilterExpressionBuilder();
//
//        FilterExpressionBuilder.Op document = b.eq(KnowledgeEnum.TYPE.getType(), DocumentEnum.DOCUMENT.getName());
//        FilterExpressionBuilder.Op userId = b.eq(KnowledgeEnum.USER_ID.getType(), BaseContext.getCurrentId());
//        FilterExpressionBuilder.Op and = b.and(document, userId);

        return QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(SearchRequest.builder()
                        .topK(5)
                        .similarityThreshold(0.1)
                        .build())
                .build();
    }


    public List<Advisor> getAllAdvisors() {
        List<Advisor> list = new ArrayList<>(getChatMemoryAdvisors());
        list.add(getQuestionAnswerAdvisors());
       return list;

    }

    public List<Advisor> getRagChatMemoryAdvisors() {

        //添加记忆功能
        List<Advisor> chatMemoryAdvisors = getChatMemoryAdvisors();



        return chatMemoryAdvisors;
    }
}
