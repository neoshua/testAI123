package com.xushu.rag.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum KnowledgeEnum {



    TYPE("type"),

    USER_ID("userId")



    ;


    //有文旦和聊天
    private String type;
}
