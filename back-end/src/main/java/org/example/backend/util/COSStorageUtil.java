package org.example.backend.util;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.region.Region;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Component
public class COSStorageUtil {

    @Value("${spring.cos.client.accessKey}")
    private String accessKey;

    @Value("${spring.cos.client.secretKey}")
    private String secretKey;

    @Value("${spring.cos.client.region}")
    private String region;

    @Value("${spring.cos.client.bucket}")
    private String bucketName;

    @Value("${spring.cos.client.basrurl}")
    private String baseUrl;

    /**
     * 上传文件到腾讯云COS
     * @param file 文件对象
     * @return 文件访问URL
     */
    public String uploadFile(MultipartFile file) throws IOException {
        // 1. 初始化COS客户端
        COSCredentials cred = new BasicCOSCredentials(accessKey, secretKey);
        ClientConfig clientConfig = new ClientConfig(new Region(region));
        clientConfig.setHttpProtocol(HttpProtocol.https);
        COSClient cosClient = new COSClient(cred, clientConfig);

        try {
            // 2. 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            String key = UUID.randomUUID().toString().replaceAll("-", "") + suffix;

            // 3. 设置元数据
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());

            // 4. 创建上传请求并执行
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key, file.getInputStream(), metadata);
            PutObjectResult putObjectResult = cosClient.putObject(putObjectRequest);

            // 5. 返回文件访问URL
            return baseUrl + "/" + key;
        } finally {
            // 6. 关闭客户端
            cosClient.shutdown();
        }
    }
}