// 阿里云VOD上传工具类
class AliyunVodUpload {
  constructor(options = {}) {
    this.userId = options.userId || 'heima';
    this.region = options.region || 'cn-shanghai';
    this.partSize = options.partSize || 1048576; // 1MB
    this.parallel = options.parallel || 5;
    this.retryCount = options.retryCount || 3;
    this.retryDuration = options.retryDuration || 2;
    this.uploader = null;
  }

  /**
   * 初始化上传实例
   * @param {Object} authInfo 授权信息
   * @param {File} file 文件对象
   * @param {Object} callbacks 回调函数
   */
  initUploader(authInfo, file, callbacks = {}) {
    return new Promise((resolve, reject) => {
      try {
        // 检查阿里云上传SDK是否可用
        if (typeof AliyunUpload === 'undefined' || typeof AliyunUpload.Vod === 'undefined') {
          reject(new Error('阿里云上传SDK未正确加载'));
          return;
        }

        // 验证必需的参数
        if (!authInfo || !authInfo.uploadAuth || !authInfo.uploadAddress) {
          reject(new Error('缺少必需的上传凭证参数'));
          return;
        }

        if (!file) {
          reject(new Error('缺少必需的文件参数'));
          return;
        }

        // 创建上传实例
        this.uploader = new AliyunUpload.Vod({
          userId: this.userId,
          region: this.region,
          partSize: this.partSize,
          parallel: this.parallel,
          retryCount: this.retryCount,
          retryDuration: this.retryDuration,
          // 上传开始回调
          onUploadstarted: (uploadInfo) => {
            console.log('上传开始:', uploadInfo);
            if (callbacks.onUploadStarted) {
              callbacks.onUploadStarted(uploadInfo);
            }
            // 设置上传凭证和地址
            this.uploader.setUploadAuthAndAddress(
              uploadInfo,
              authInfo.uploadAuth,
              authInfo.uploadAddress,
              authInfo.videoId
            );
          },
          // 上传进度回调
          onUploadProgress: (uploadInfo, totalSize, loadedSize) => {
            console.log('上传进度:', uploadInfo, totalSize, loadedSize);
            if (callbacks.onUploadProgress) {
              callbacks.onUploadProgress(uploadInfo, totalSize, loadedSize);
            }
          },
          // 上传成功回调
          onUploadSucceed: (uploadInfo) => {
            console.log('上传成功:', uploadInfo);
            if (callbacks.onUploadSucceed) {
              callbacks.onUploadSucceed(uploadInfo);
            }
            resolve(uploadInfo);
          },
          // 上传失败回调
          onUploadFailed: (uploadInfo, code, message) => {
            console.error('上传失败:', uploadInfo, code, message);
            if (callbacks.onUploadFailed) {
              callbacks.onUploadFailed(uploadInfo, code, message);
            }
            reject(new Error(message));
          },
          // 上传结束回调
          onUploadEnd: (uploadInfo) => {
            console.log('上传结束:', uploadInfo);
            if (callbacks.onUploadEnd) {
              callbacks.onUploadEnd(uploadInfo);
            }
          },
          // 上传凭证过期回调
          onUploadTokenExpired: (uploadInfo) => {
            console.warn('上传凭证过期:', uploadInfo);
            if (callbacks.onUploadTokenExpired) {
              callbacks.onUploadTokenExpired(uploadInfo);
            }
            // 这里应该调用刷新凭证的方法
            // this.refreshUploadAuth(uploadInfo.videoId);
          }
        });

        // 添加文件到上传队列
        this.uploader.addFile(file);

        resolve(this.uploader);
      } catch (error) {
        reject(new Error('初始化上传实例失败: ' + error.message));
      }
    });
  }

  /**
   * 开始上传
   */
  startUpload() {
    if (!this.uploader) {
      throw new Error('上传实例未初始化');
    }
    this.uploader.startUpload();
  }

  /**
   * 停止上传
   */
  stopUpload() {
    if (this.uploader) {
      this.uploader.stopUpload();
    }
  }

  /**
   * 恢复上传（凭证过期时使用）
   * @param {string} uploadAuth 新的上传凭证
   */
  resumeUploadWithAuth(uploadAuth) {
    if (this.uploader) {
      this.uploader.resumeUploadWithAuth(uploadAuth);
    }
  }

  /**
   * 检查SDK是否可用
   */
  static isSdkAvailable() {
    return typeof AliyunUpload !== 'undefined' && typeof AliyunUpload.Vod !== 'undefined';
  }
}

export default AliyunVodUpload;
