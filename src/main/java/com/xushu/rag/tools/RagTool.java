package com.xushu.rag.tools;

import com.aispace.supersql.builder.RagOptions;
import com.aispace.supersql.engine.SpringSqlEngine;
import com.alibaba.fastjson.JSON;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service
public class RagTool {


    @Resource
    private SpringSqlEngine springSqlEngine;
    @Resource
    private ChatModel dashscopeChatModel;



    @Tool(description = "涉及统计数据、求和、计数、平均值等聚合操作")
    public String getAggregationQuery(@ToolParam(description = "用户的提问") String question) {
        // 是聚合对话
        // 使用 SuperSQL 的 text-to-sql 功能生成实际的 SQL 查询
        String s = springSqlEngine.setChatModel(dashscopeChatModel)
                .setOptions(RagOptions.builder().topN(10).rerank(false).limitScore(0.1).build())
                .generateSql(question);
        List<Map<String, Object>> maps = springSqlEngine.executeSql(s);


        return JSON.toJSONString( maps);
    }
}
