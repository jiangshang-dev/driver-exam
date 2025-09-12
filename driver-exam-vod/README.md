# 驾考系统视频点播服务模块 (driver-exam-vod)

## 简介

本模块是驾考系统中专门用于处理视频点播服务的独立模块，集成了阿里云视频点播服务(VOD)，支持视频上传、管理、播放等功能。

## 功能特性

1. **阿里云VOD集成** - 完整集成阿里云视频点播服务
2. **高并发支持** - 支持100人同时上传视频
3. **动态配置** - 支持运行时动态调整配置参数
4. **缓存优化** - 使用Redis缓存提升性能
5. **管理后台** - 提供完整的配置和视频管理接口

## 技术栈

- Spring Boot 3.x
- MyBatis Plus
- Redis
- 阿里云VOD SDK
- MySQL

## 模块结构

```
driver-exam-vod/
├── src/main/java/com/heima/vod
│   ├── config/           # 配置类
│   ├── controller/       # 控制器
│   ├── dao/              # 数据访问对象
│   ├── entity/           # 实体类
│   ├── service/          # 服务接口
│   │   └── impl/         # 服务实现
│   └── DriverExamVodApplication.java  # 启动类
└── src/main/resources/
    ├── db/               # 数据库脚本
    ├── mapper/           # MyBatis映射文件
    └── application.yml   # 配置文件
```

## 核心服务

1. **AliyunVodService** - 阿里云VOD核心服务接口
2. **VodConfigService** - 配置管理服务
3. **VideoInfoService** - 视频信息管理服务

## API接口

### 视频上传相关
- `POST /vod/upload/auth` - 获取视频上传地址和凭证
- `POST /vod/upload/refresh` - 刷新视频上传凭证

### 视频播放相关
- `GET /vod/play/info` - 获取视频播放地址
- `GET /vod/play/auth` - 获取视频播放凭证

### 视频管理相关
- `PUT /vod/video` - 更新视频信息
- `DELETE /vod/video` - 删除视频

### 管理后台接口
- `GET/POST /admin/vod/config/aliyun` - 阿里云VOD配置管理
- `GET/POST /admin/vod/config/video` - 视频业务配置管理
- `GET /admin/vod/video/list` - 视频列表查询
- `POST /admin/vod/video/status` - 更新视频状态

## 部署说明

1. 确保MySQL和Redis服务正常运行
2. 执行数据库脚本创建表结构
3. 修改`application.yml`配置文件中的数据库和Redis连接信息
4. 运行`DriverExamVodApplication`启动服务

## 配置说明

在管理后台中配置以下信息：

1. **阿里云VOD配置**：
   - AccessKey ID
   - AccessKey Secret
   - 区域ID
   - 接入点

2. **视频业务配置**：
   - 最大并发上传数
   - 默认转码模板
   - 默认水印配置
   - 存储位置

## 使用示例

### 1. 获取上传凭证
```bash
curl -X POST "http://localhost:8082/vod/upload/auth?title=测试视频&fileName=test.mp4&fileSize=1024"
```

### 2. 获取播放凭证
```bash
curl -X GET "http://localhost:8082/vod/play/auth?videoId=1234567890"
```

## 注意事项

1. 请妥善保管阿里云AccessKey，建议使用子账号并分配最小权限
2. 根据实际需求调整并发上传数量
3. 定期清理不需要的视频以节省存储空间