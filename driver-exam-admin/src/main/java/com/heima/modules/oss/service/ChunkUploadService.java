package com.heima.modules.oss.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.heima.common.exception.RenException;
import com.heima.modules.oss.cloud.OSSFactory;
import com.heima.modules.oss.entity.SysOssEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 大文件分片上传：初始化、上传分片、断点续传状态、合并分片并上传到当前配置的文件服务器
 */
@Slf4j
@Service
public class ChunkUploadService {

    private static final long DEFAULT_CHUNK_SIZE = 5L * 1024 * 1024;
    private static final long MAX_CHUNK_SIZE = 90L * 1024 * 1024;
    private static final String META_FILE = "meta.json";

    /** 允许上传的类型：视频 + 常见文档/压缩包（视频在线播放，其他文件在线预览/下载） */
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "mp4", "mov", "avi", "mkv", "flv", "wmv", "webm", "m4v", "mpeg", "mpg", "ts", "3gp",
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "csv", "md", "zip", "rar", "7z");

    private final File rootDir = new File(System.getProperty("java.io.tmpdir"), "driver-exam-chunks");

    private final SysOssService sysOssService;

    public ChunkUploadService(SysOssService sysOssService) {
        this.sysOssService = sysOssService;
    }

    /**
     * 初始化上传任务，返回 uploadId 和已上传的分片（支持断点续传）
     */
    public Map<String, Object> init(String fileName, long fileSize, long chunkSize, String identifier) {
        if (StrUtil.isBlank(fileName)) {
            throw new RenException("文件名不能为空");
        }
        if (fileSize <= 0) {
            throw new RenException("文件大小不能为空");
        }
        String extension = FileUtil.extName(fileName);
        if (StrUtil.isBlank(extension) || !ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new RenException("不支持的文件类型，仅支持视频及常见文档/压缩包");
        }
        if (chunkSize <= 0) {
            chunkSize = DEFAULT_CHUNK_SIZE;
        }
        if (chunkSize > MAX_CHUNK_SIZE) {
            chunkSize = MAX_CHUNK_SIZE;
        }

        long totalChunks = (fileSize + chunkSize - 1) / chunkSize;
        String rawKey = StrUtil.blankToDefault(identifier, "") + "|" + fileName + "|" + fileSize + "|" + chunkSize;
        String uploadId = SecureUtil.md5(rawKey);

        File dir = new File(rootDir, uploadId);
        File metaFile = new File(dir, META_FILE);
        if (!metaFile.exists()) {
            FileUtil.mkdir(dir);
            JSONObject meta = new JSONObject();
            meta.set("fileName", fileName);
            meta.set("fileSize", fileSize);
            meta.set("chunkSize", chunkSize);
            meta.set("totalChunks", totalChunks);
            meta.set("extension", extension.toLowerCase());
            FileUtil.writeUtf8String(meta.toString(), metaFile);
        }

        return buildStatus(uploadId, dir, chunkSize, (int) totalChunks);
    }

    /**
     * 上传单个分片
     */
    public Map<String, Object> uploadChunk(String uploadId, int index, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RenException("分片内容不能为空");
        }
        File dir = getTaskDir(uploadId);
        JSONObject meta = readMeta(dir);
        int totalChunks = meta.getInt("totalChunks");
        if (index < 0 || index >= totalChunks) {
            throw new RenException("分片序号不合法");
        }
        File partFile = new File(dir, index + ".part");
        try {
            file.transferTo(partFile.getAbsoluteFile());
        } catch (Exception e) {
            log.error("保存分片失败, uploadId={}, index={}", uploadId, index, e);
            throw new RenException("保存分片失败：" + e.getMessage());
        }
        return buildStatus(uploadId, dir, meta.getLong("chunkSize"), totalChunks);
    }

    /**
     * 查询上传进度（断点续传）
     */
    public Map<String, Object> status(String uploadId) {
        File dir = getTaskDir(uploadId);
        JSONObject meta = readMeta(dir);
        return buildStatus(uploadId, dir, meta.getLong("chunkSize"), meta.getInt("totalChunks"));
    }

    /**
     * 合并分片并上传到当前配置的文件服务器（MinIO/RustFS/OSS 等）
     */
    public synchronized Map<String, Object> merge(String uploadId) {
        File dir = getTaskDir(uploadId);
        JSONObject meta = readMeta(dir);
        int totalChunks = meta.getInt("totalChunks");
        String extension = meta.getStr("extension");
        String fileName = meta.getStr("fileName");

        for (int i = 0; i < totalChunks; i++) {
            if (!new File(dir, i + ".part").exists()) {
                throw new RenException("分片不完整，缺少第 " + (i + 1) + " 个分片");
            }
        }

        File mergedFile = new File(dir, "merged." + extension);
        long startTime = System.currentTimeMillis();
        try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(mergedFile.toPath()))) {
            for (int i = 0; i < totalChunks; i++) {
                Files.copy(new File(dir, i + ".part").toPath(), out);
            }
        } catch (Exception e) {
            log.error("合并分片失败, uploadId={}", uploadId, e);
            throw new RenException("合并分片失败：" + e.getMessage());
        }

        String url;
        try (InputStream in = new FileInputStream(mergedFile)) {
            url = OSSFactory.build().uploadSuffix(in, extension, mergedFile.length());
        } catch (RenException e) {
            throw e;
        } catch (Exception e) {
            log.error("上传合并文件失败, uploadId={}", uploadId, e);
            throw new RenException("上传文件失败：" + e.getMessage());
        }

        // 记录到文件上传列表
        SysOssEntity ossEntity = new SysOssEntity();
        ossEntity.setUrl(url);
        ossEntity.setCreateDate(new Date());
        sysOssService.insert(ossEntity);

        long mergedSize = mergedFile.length();
        FileUtil.del(dir);
        log.info("大文件合并上传完成, uploadId={}, fileName={}, size={}, cost={}ms",
                uploadId, fileName, mergedSize, System.currentTimeMillis() - startTime);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("url", url);
        result.put("fileName", fileName);
        return result;
    }

    /**
     * 取消上传并清理分片
     */
    public void cancel(String uploadId) {
        File dir = getTaskDir(uploadId);
        FileUtil.del(dir);
    }

    private Map<String, Object> buildStatus(String uploadId, File dir, long chunkSize, int totalChunks) {
        List<Integer> uploaded = new ArrayList<>();
        for (int i = 0; i < totalChunks; i++) {
            if (new File(dir, i + ".part").exists()) {
                uploaded.add(i);
            }
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("uploadId", uploadId);
        result.put("chunkSize", chunkSize);
        result.put("totalChunks", totalChunks);
        result.put("uploaded", uploaded);
        return result;
    }

    private File getTaskDir(String uploadId) {
        if (StrUtil.isBlank(uploadId) || !uploadId.matches("[a-fA-F0-9]{32}")) {
            throw new RenException("上传ID不合法");
        }
        File dir = new File(rootDir, uploadId);
        if (!dir.exists()) {
            throw new RenException("上传任务不存在或已过期");
        }
        return dir;
    }

    private JSONObject readMeta(File dir) {
        File metaFile = new File(dir, META_FILE);
        if (!metaFile.exists()) {
            throw new RenException("上传任务不存在或已过期");
        }
        return JSONUtil.parseObj(FileUtil.readUtf8String(metaFile));
    }
}
