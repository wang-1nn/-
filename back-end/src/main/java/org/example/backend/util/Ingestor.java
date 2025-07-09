package org.example.backend.util;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 工具类：将本地文件写入向量库，并返回完整文本内容，方便后续拼接 Prompt。
 */
@Component
@RequiredArgsConstructor
public class
Ingestor {

    @Autowired
    VectorStore vectorStore;

    /**
     * MultipartFile 入口（表单上传）。
     */
    public String ingestMultipartFile(MultipartFile mf) {
        try {
            // 确保有文件名，防止 TextReader 写入 null metadata
            String safeName = (mf.getOriginalFilename() != null && !mf.getOriginalFilename().isBlank())
                    ? mf.getOriginalFilename()
                    : "uploaded-" + System.currentTimeMillis() + ".txt";

            Resource res = new ByteArrayResource(mf.getBytes(), safeName) {
                @Override
                public String getFilename() {
                    return safeName;
                }
            };
            return ingestTextResource(res);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 核心实现：读 → 切块 → 写入向量库，最后返回文件全文。
     */
    public String ingestTextResource(Resource resource) {
        try {
            // 1. 读取全文（TextReader 会按整行读）
            TikaDocumentReader reader = new TikaDocumentReader(resource);
            List<Document> docs = reader.read();
            if (resource.getFilename() != null) {
                for (Document doc : docs) {
                    doc.getMetadata().put("filename", resource.getFilename());
                }
            }
            // 2. 聚合全文
            StringBuilder fullText = new StringBuilder();
            for (Document d : docs) {
                if (d.isText() && d.getText() != null) {
                    fullText.append(d.getText()).append("\n");
                }
            }

            // 3. 切块写库
            TokenTextSplitter splitter = TokenTextSplitter.builder()
                    .withChunkSize(500)
                    .withKeepSeparator(false)
                    .build();
            List<Document> chunks = splitter.apply(docs);
            vectorStore.write(chunks);
            System.out.printf("✅ 向量化完成: %s, %d 块\n", resource.getFilename(), chunks.size());

            return fullText.toString();
        } catch (Exception e) {
            System.err.printf("❌ ingestResource 出错, file=%s, msg=%s\n", resource.getFilename(), e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
