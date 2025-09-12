# 视频点播模块使用指南

## 1. 模块简介

视频点播模块(driver-exam-vod)是驾考系统中专门用于处理视频相关功能的独立服务模块，集成了阿里云视频点播服务(VOD)，支持视频上传、管理、播放等功能。

## 2. 部署步骤

### 2.1 环境准备
1. 确保已安装JDK 17+
2. 确保已安装MySQL 8.0+
3. 确保已安装Redis
4. 确保已创建数据库driver_exam

### 2.2 数据库初始化
1. 执行`src/main/resources/db/vod.sql`创建视频相关表
2. 执行`src/main/resources/db/menu.sql`添加菜单项和权限

### 2.3 配置修改
1. 修改`src/main/resources/application.yml`中的数据库连接配置
2. 修改`src/main/resources/application.yml`中的Redis连接配置

### 2.4 启动服务
1. 运行`DriverExamVodApplication.java`启动视频服务
2. 默认端口为8082

## 3. 配置阿里云VOD

### 3.1 获取阿里云AccessKey
1. 登录阿里云控制台
2. 进入AccessKey管理页面
3. 创建子账号并获取AccessKey ID和AccessKey Secret

### 3.2 配置VOD服务
1. 启动服务后，访问管理后台的视频配置页面
2. 填入阿里云AccessKey ID和AccessKey Secret
3. 根据需要调整区域ID和接入点
4. 保存配置

## 4. 使用视频上传功能

### 4.1 前端集成
1. 在前端项目中添加视频上传页面组件
2. 调用视频服务的API接口实现上传功能

### 4.2 上传流程
1. 调用`/vod/upload/auth`接口获取上传地址和凭证
2. 使用阿里云上传SDK进行视频上传
3. 上传完成后，系统会自动更新视频状态

## 5. API接口说明

### 5.1 视频上传相关接口
- **获取上传凭证**
  - URL: `POST /vod/upload/auth`
  - 参数: title(视频标题), fileName(文件名), fileSize(文件大小)
  - 返回: videoId(视频ID), uploadAddress(上传地址), uploadAuth(上传凭证)

- **刷新上传凭证**
  - URL: `POST /vod/upload/refresh`
  - 参数: videoId(视频ID)
  - 返回: uploadAddress(上传地址), uploadAuth(上传凭证)

### 5.2 视频播放相关接口
- **获取播放地址**
  - URL: `GET /vod/play/info`
  - 参数: videoId(视频ID)
  - 返回: videoBase(视频基本信息), playInfoList(播放信息列表)

- **获取播放凭证**
  - URL: `GET /vod/play/auth`
  - 参数: videoId(视频ID)
  - 返回: playAuth(播放凭证), videoMeta(视频元信息)

### 5.3 视频管理相关接口
- **更新视频信息**
  - URL: `PUT /vod/video`
  - 参数: videoId(视频ID), title(标题), description(描述), tags(标签)
  - 返回: 更新结果

- **删除视频**
  - URL: `DELETE /vod/video`
  - 参数: videoId(视频ID)
  - 返回: 删除结果

### 5.4 管理后台接口
- **配置管理**
  - 获取阿里云VOD配置: `GET /admin/vod/config/aliyun`
  - 保存阿里云VOD配置: `POST /admin/vod/config/aliyun`
  - 获取视频业务配置: `GET /admin/vod/config/video`
  - 保存视频业务配置: `POST /admin/vod/config/video`

- **视频管理**
  - 获取视频列表: `GET /admin/vod/video/list`
  - 更新视频状态: `POST /admin/vod/video/status`
  - 删除视频: `DELETE /admin/vod/video/{id}`

## 6. 性能优化建议

### 6.1 并发控制
1. 根据服务器性能调整最大并发上传数
2. 监控系统资源使用情况，适时调整配置

### 6.2 缓存优化
1. 合理设置Redis缓存过期时间
2. 对频繁访问的配置信息进行缓存

### 6.3 数据库优化
1. 为常用查询字段添加索引
2. 定期清理过期数据

## 7. 安全建议

### 7.1 AccessKey管理
1. 使用子账号AccessKey，避免使用主账号
2. 定期轮换AccessKey
3. 为子账号分配最小必要权限

### 7.2 数据安全
1. 对敏感信息进行加密存储
2. 定期备份重要数据
3. 设置合理的数据访问权限

## 8. 故障排查

### 8.1 上传失败
1. 检查阿里云AccessKey是否正确
2. 检查网络连接是否正常
3. 检查上传凭证是否过期

### 8.2 播放异常
1. 检查视频状态是否正常
2. 检查播放凭证是否过期
3. 检查网络连接是否正常

### 8.3 性能问题
1. 检查服务器资源使用情况
2. 检查数据库连接池配置
3. 检查Redis缓存命中率