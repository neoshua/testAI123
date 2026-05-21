package com.xushu.rag.config;


import com.xushu.rag.constant.ChatModelEnum;
import com.xushu.rag.tools.RagTool;
import com.xushu.rag.utils.ChatClientFactory;
import io.modelcontextprotocol.server.transport.WebFluxStreamableServerTransportProvider;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import javax.swing.*;
import java.util.List;

@Configuration
public class ChatConfig {


    @Resource
    private AdaptiveManager adaptiveManager;



    @Bean
    public ChatClient dashScopeChatclient(ChatModel dashscopeChatModel) {

        List<Advisor> advisors = adaptiveManager.getChatMemoryAdvisors();
        ChatClient dashScopeChatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem("你是一个乐于助人解决问题的AI机器人")
                .defaultAdvisors(advisors)
                .build();
        ChatClientFactory.CHAT_CLIENT_MAP.put(ChatModelEnum.DASH_SCOPE, dashScopeChatClient);
        return dashScopeChatClient;
    }


    @Bean
    public ChatClient ollamaChatclient(ChatModel ollamaChatModel) {
        List<Advisor> advisors = adaptiveManager.getChatMemoryAdvisors();
        ChatClient ollamaChatclient = ChatClient.builder(ollamaChatModel)
                .defaultSystem("你是一个乐于助人解决问题的AI机器人")
                .defaultAdvisors(advisors)
                .build();
        ChatClientFactory.CHAT_CLIENT_MAP.put(ChatModelEnum.OLLAMA, ollamaChatclient);
        return ollamaChatclient;
    }



    @Bean
    public ChatClient ragChatclient(ChatModel dashscopeChatModel, RagTool ragTool, SyncMcpToolCallbackProvider toolCallbackProvider) {
          String DEFAULT_SYSTEM_PROMPT = """
                        你是"XS"知识库系统的对话助手，请以乐于助人的方式进行对话，
                        {rag_message}
                        今天的日期：{current_data}
                        """;
        List<Advisor> advisors = adaptiveManager.getRagChatMemoryAdvisors();
         ChatClient dashScopeChatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(DEFAULT_SYSTEM_PROMPT)
                .defaultSystem(p -> p.param("rag_message", ""))
                .defaultAdvisors(advisors)
                .defaultToolCallbacks(toolCallbackProvider)
                .defaultTools(ragTool)
                 .build();

        ChatClientFactory.CHAT_CLIENT_MAP.put(ChatModelEnum.RAG, dashScopeChatClient);
        return dashScopeChatClient;
    }

}
