package com.heima.modules.oss.cloud;

/**
 * MinIO 存储，走 S3 协议。
 */
public class MinioCloudStorageService extends S3CloudStorageService {

    public MinioCloudStorageService(CloudStorageConfig config) {
        super(config, config.getMinioEndPoint(), config.getMinioAccessKey(), config.getMinioSecretKey(),
                config.getMinioBucketName(), config.getMinioDomain(), config.getMinioPrefix());
    }
}
