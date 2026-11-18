package com.heima.modules.oss.cloud;

import cn.hutool.core.util.StrUtil;
import com.heima.common.exception.ErrorCode;
import com.heima.common.exception.RenException;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;

/**
 * S3 协议存储。MinIO 和 RustFS 共用这一套上传。
 */
public class S3CloudStorageService extends AbstractCloudStorageService {

    private final String endpoint;
    private final String accessKey;
    private final String secretKey;
    private final String bucket;
    private final String domain;
    private final String prefix;

    public S3CloudStorageService(CloudStorageConfig config, String endpoint, String accessKey, String secretKey,
                                 String bucket, String domain, String prefix) {
        this.config = config;
        this.endpoint = endpoint;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.bucket = bucket;
        this.domain = domain;
        this.prefix = prefix;
    }

    @Override
    public String upload(byte[] data, String path) {
        return upload(new ByteArrayInputStream(data), path, data.length);
    }

    @Override
    public String upload(InputStream inputStream, String path) {
        try {
            byte[] data = inputStream.readAllBytes();
            return upload(data, path);
        } catch (RenException e) {
            throw e;
        } catch (Exception e) {
            throw new RenException(ErrorCode.OSS_UPLOAD_FILE_ERROR, e, "");
        }
    }

    @Override
    public String upload(InputStream inputStream, String path, long contentLength) {
        String endpointUrl = normalizeEndpoint(endpoint);
        try (S3Client client = S3Client.builder()
                .endpointOverride(URI.create(endpointUrl))
                .region(Region.US_EAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
                .httpClient(UrlConnectionHttpClient.create())
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
                .build()) {
            client.putObject(PutObjectRequest.builder().bucket(bucket).key(path).build(),
                    RequestBody.fromInputStream(inputStream, contentLength));
        } catch (Exception e) {
            throw new RenException(ErrorCode.OSS_UPLOAD_FILE_ERROR, e, "");
        }
        return buildUrl(endpointUrl, path);
    }

    @Override
    public String uploadSuffix(byte[] data, String suffix) {
        return upload(data, getPath(prefix, suffix));
    }

    @Override
    public String uploadSuffix(InputStream inputStream, String suffix) {
        return upload(inputStream, getPath(prefix, suffix));
    }

    private String buildUrl(String endpointUrl, String path) {
        String base = StrUtil.isNotBlank(domain) ? StrUtil.removeSuffix(domain.trim(), "/") : endpointUrl + "/" + bucket;
        return base + "/" + path;
    }

    private String normalizeEndpoint(String value) {
        String endpointUrl = StrUtil.removeSuffix(StrUtil.trim(value), "/");
        if (!StrUtil.startWithIgnoreCase(endpointUrl, "http://") && !StrUtil.startWithIgnoreCase(endpointUrl, "https://")) {
            endpointUrl = "http://" + endpointUrl;
        }
        return endpointUrl;
    }
}
