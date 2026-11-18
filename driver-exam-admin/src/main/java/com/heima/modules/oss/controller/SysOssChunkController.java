package com.heima.modules.oss.controller;

import com.heima.common.annotation.LogOperation;
import com.heima.common.utils.Result;
import com.heima.modules.oss.service.ChunkUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 大文件分片上传（断点续传、合并分片）
 */
@RestController
@RequestMapping("sys/oss/chunk")
@Tag(name = "大文件分片上传")
@AllArgsConstructor
public class SysOssChunkController {

    private final ChunkUploadService chunkUploadService;

    @PostMapping("init")
    @Operation(summary = "初始化上传任务")
    @RequiresPermissions("sys:oss:all")
    public Result<Map<String, Object>> init(@RequestBody Map<String, Object> params) {
        String fileName = params.get("fileName") == null ? null : params.get("fileName").toString();
        long fileSize = parseLong(params.get("fileSize"), 0);
        long chunkSize = parseLong(params.get("chunkSize"), 0);
        String identifier = params.get("identifier") == null ? null : params.get("identifier").toString();

        return new Result<Map<String, Object>>().ok(chunkUploadService.init(fileName, fileSize, chunkSize, identifier));
    }

    @PostMapping("upload")
    @Operation(summary = "上传分片")
    @RequiresPermissions("sys:oss:all")
    public Result<Map<String, Object>> upload(@RequestParam("file") MultipartFile file,
                                              @RequestParam("uploadId") String uploadId,
                                              @RequestParam("index") Integer index) {
        return new Result<Map<String, Object>>().ok(chunkUploadService.uploadChunk(uploadId, index, file));
    }

    @GetMapping("status")
    @Operation(summary = "查询上传进度")
    @RequiresPermissions("sys:oss:all")
    public Result<Map<String, Object>> status(@RequestParam("uploadId") String uploadId) {
        return new Result<Map<String, Object>>().ok(chunkUploadService.status(uploadId));
    }

    @PostMapping("merge")
    @Operation(summary = "合并分片并上传")
    @LogOperation("合并分片上传")
    @RequiresPermissions("sys:oss:all")
    public Result<Map<String, Object>> merge(@RequestBody Map<String, Object> params) {
        String uploadId = params.get("uploadId") == null ? null : params.get("uploadId").toString();
        return new Result<Map<String, Object>>().ok(chunkUploadService.merge(uploadId));
    }

    @PostMapping("cancel")
    @Operation(summary = "取消上传")
    @RequiresPermissions("sys:oss:all")
    public Result cancel(@RequestBody Map<String, Object> params) {
        String uploadId = params.get("uploadId") == null ? null : params.get("uploadId").toString();
        chunkUploadService.cancel(uploadId);
        return new Result();
    }

    private static long parseLong(Object value, long defaultValue) {
        try {
            return value == null ? defaultValue : Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
