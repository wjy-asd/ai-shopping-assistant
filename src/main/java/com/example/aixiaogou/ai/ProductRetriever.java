package com.example.aixiaogou.ai;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.example.aixiaogou.entity.ProductEntity;
import com.example.aixiaogou.service.ProductService;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;

/**
 * 商品知识库检索器（RAG）。
 *
 * 原理：把商品信息构造为"知识片段"，查询时先把问题向量化，
 * 再与每个知识片段做余弦相似度比较，返回最相关的 top3 片段作为回答依据。
 */
public class ProductRetriever implements ContentRetriever {

    private final EmbeddingModel embeddingModel;
    private final ProductService productService;

    /** 知识片段：每件商品一段文本 */
    private volatile List<TextSegment> knowledgeChunks;
    private volatile List<Embedding> chunkEmbeddings;

    public ProductRetriever(EmbeddingModel embeddingModel, ProductService productService) {
        this.embeddingModel = embeddingModel;
        this.productService = productService;
    }

    private void ensureKnowledge() {
        if (knowledgeChunks == null) {
            synchronized (this) {
                if (knowledgeChunks == null) {
                    List<ProductEntity> products = productService.listProducts();
                    List<TextSegment> chunks = new ArrayList<>();
                    for (ProductEntity p : products) {
                        chunks.add(TextSegment.from(String.format("%s（%s），价格 %.2f 元，库存 %d 件。%s",
                                p.getName(), p.getCategory(), p.getPrice(), p.getStock(),
                                p.getDescription())));
                    }
                    knowledgeChunks = chunks;
                    chunkEmbeddings = embeddingModel.embedAll(chunks).content();
                }
            }
        }
    }

    @Override
    public List<Content> retrieve(Query query) {
        ensureKnowledge();
        // 1. 查询文本向量化
        float[] queryVector = embeddingModel.embed(query.text()).content().vector();

        // 2. 与每个知识片段计算余弦相似度
        List<double[]> scores = new ArrayList<>();
        for (int i = 0; i < knowledgeChunks.size(); i++) {
            double score = cosineSimilarity(queryVector, chunkEmbeddings.get(i).vector());
            scores.add(new double[]{score, i});
        }

        // 3. 按相似度降序取 top3
        scores.sort(Comparator.comparingDouble((double[] s) -> s[0]).reversed());
        List<Content> results = new ArrayList<>();
        for (int k = 0; k < Math.min(3, scores.size()); k++) {
            if (scores.get(k)[0] <= 0) {
                break;
            }
            results.add(Content.from(knowledgeChunks.get((int) scores.get(k)[1]).text()));
        }
        return results;
    }

    private double cosineSimilarity(float[] vecA, float[] vecB) {
        double dot = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < vecA.length; i++) {
            dot += vecA[i] * vecB[i];
            normA += vecA[i] * vecA[i];
            normB += vecB[i] * vecB[i];
        }
        if (normA == 0.0 || normB == 0.0) {
            return 0.0;
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
