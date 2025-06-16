package org.example.backend.util;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TextIngestorUtil {
    @Autowired
    VectorStore vectorStore;          // Neo4jVectorStore

    /** 应用启动后自动调用；只跑一次 */
//    @PostConstruct
    public void ingest() {
        List<Document> docs = read();

        // 可选：按 token 切块
        TokenTextSplitter splitter = TokenTextSplitter.builder()
                .withChunkSize(500)   // ← 用 withChunkSize
                .withKeepSeparator(false)
                .build();
        List<Document> chunks = splitter.apply(docs);

        vectorStore.write(chunks);  // 自动 Embedding -> Neo4j
        System.out.println("✅ text-source.txt 已写入向量库");
    }

    @Autowired
    Resource textSource;   // 在 RagConfig 里通过 @Value 注入

    public List<Document> read() {
        TextReader reader = new TextReader(textSource);
        reader.getCustomMetadata().put("filename", textSource.getFilename());
        return reader.read();            // Spring AI 1.0 用 read()
    }
}
