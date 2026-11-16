package com.heima.modules.oss.cloud;

/**
 * RustFS 存储，走 S3 协议。
 */
public class RustfsCloudStorageService extends S3CloudStorageService {

    public RustfsCloudStorageService(CloudStorageConfig config) {
        super(config, config.getRustfsEndPoint(), config.getRustfsAccessKey(), config.getRustfsSecretKey(),
                config.getRustfsBucketName(), config.getRustfsDomain(), config.getRustfsPrefix());
    }
}