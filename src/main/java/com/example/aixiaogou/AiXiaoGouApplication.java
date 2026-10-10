package com.example.aixiaogou;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 实验8：LangChain4j 入门、AIService、聊天记忆持久化、提示词工程
 * 项目：AI小购 - 电商智能购物助手
 */
@SpringBootApplication
@MapperScan("com.example.aixiaogou.mapper")
public class AiXiaoGouApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiXiaoGouApplication.class, args);
    }
}
