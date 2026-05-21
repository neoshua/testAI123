package com.xushu.rag;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(
)
@EnableScheduling
@MapperScan("com.xushu.rag.mapper")

public class XushuRagAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(XushuRagAiApplication.class, args);
    }

}
