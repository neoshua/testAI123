package com.xushu.rag.constant;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ChatModelEnum {
    DASH_SCOPE("dashScope","千问"),
    OLLAMA("ollama","ollama"),
    RAG("rag","rag"),
    ;

    private String model;
    private String desc;


}
