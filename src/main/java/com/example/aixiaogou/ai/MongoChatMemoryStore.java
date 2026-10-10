package com.example.aixiaogou.ai;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;

/**
 * 任务5：MongoDB 聊天记忆持久化实现。
 *
 * ChatMemoryStore 是 LangChain4j 记忆持久化的 SPI：
 *  - getMessages(memoryId)    根据会话 ID 读取历史消息；
 *  - updateMessages(...)      写入（覆盖）该会话的最新消息列表；
 *  - deleteMessages(memoryId) 删除会话。
 *
 * 数据以 JSON 形式存放在 MongoDB 的 chat_memory 集合中，
 * 一条文档对应一个会话（chatMemoryId），应用重启后记忆不丢失。
 */
public class MongoChatMemoryStore implements ChatMemoryStore {

    public static final String COLLECTION = "chat_memory";

    private final MongoTemplate mongoTemplate;

    public MongoChatMemoryStore(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        MongoMemoryDoc doc = mongoTemplate.findById(
                memoryId.toString(), MongoMemoryDoc.class, COLLECTION);
        if (doc == null || doc.getMessagesJson() == null || doc.getMessagesJson().isBlank()) {
            return new ArrayList<>();
        }
        // 从 JSON 反序列化回消息列表
        return ChatMessageDeserializer.messagesFromJson(doc.getMessagesJson());
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        MongoMemoryDoc doc = new MongoMemoryDoc();
        doc.setId(memoryId.toString());
        // 把消息列表序列化为 JSON 后整体存储
        doc.setMessagesJson(ChatMessageSerializer.messagesToJson(messages));
        mongoTemplate.save(doc, COLLECTION);
    }

    @Override
    public void deleteMessages(Object memoryId) {
        mongoTemplate.remove(
                Query.query(Criteria.where("_id").is(memoryId.toString())),
                COLLECTION);
    }

    /** MongoDB 文档模型：一个会话一条文档 */
    public static class MongoMemoryDoc {

        @Id
        private String id;

        private String messagesJson;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getMessagesJson() {
            return messagesJson;
        }

        public void setMessagesJson(String messagesJson) {
            this.messagesJson = messagesJson;
        }
    }
}
