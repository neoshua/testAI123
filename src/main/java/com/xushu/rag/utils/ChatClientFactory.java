package com.xushu.rag.utils;

import com.xushu.rag.constant.ChatModelEnum;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;

import java.util.HashMap;
import java.util.Map;

public class ChatClientFactory {


    public static final Map<ChatModelEnum,ChatClient>  CHAT_CLIENT_MAP= new HashMap<>();



    public static ChatClient getChatClient(ChatModelEnum chatModelEnum) {


        return  CHAT_CLIENT_MAP.get(chatModelEnum);
    }
    public static ChatClient getChatClient() {
        return  getChatClient(ChatModelEnum.DASH_SCOPE);
    }



}
