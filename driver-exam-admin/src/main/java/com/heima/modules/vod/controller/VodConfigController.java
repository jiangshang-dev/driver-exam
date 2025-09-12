package com.heima.modules.vod.controller;

import com.heima.common.utils.Result;
import com.heima.modules.sys.dto.SysDeptDTO;
import com.heima.vod.entity.VodConfigEntity;
import com.heima.vod.entity.VideoConfigEntity;
import com.heima.vod.service.VodConfigService;
import com.heima.vod.service.VideoConfigService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * VOD配置管理控制器
 */
@RestController
@RequestMapping("/admin/vod/config")
public class VodConfigController {
    
    @Resource
    private VodConfigService vodConfigService;
    
    @Resource
    private VideoConfigService videoConfigService;
    
    /**
     * 获取阿里云VOD配置
     * @return 配置信息
     */
    @GetMapping("/aliyun")
    public Result<VodConfigEntity> getVodConfig() {
        try {
            VodConfigEntity config = vodConfigService.getEnabledConfig();
            return new Result<VodConfigEntity>().ok(config);
        } catch (Exception e) {
            return new Result<VodConfigEntity>().error();
        }
    }
    
    /**
     * 保存阿里云VOD配置
     * @param config 配置信息
     * @return 保存结果
     */
    @PostMapping("/aliyun")
    public Result<VodConfigEntity> saveVodConfig(@RequestBody VodConfigEntity config) {
        try {
            // 如果是启用状态，先禁用其他配置
            if (config.getEnable() != null && config.getEnable()) {
                VodConfigEntity enabledConfig = vodConfigService.getEnabledConfig();
                if (enabledConfig != null && !enabledConfig.getId().equals(config.getId())) {
                    enabledConfig.setEnable(false);
                    vodConfigService.updateById(enabledConfig);
                }
            }
            
            if (config.getId() == null) {
                vodConfigService.save(config);
            } else {
                vodConfigService.updateById(config);
            }

            return new Result<VodConfigEntity>().ok(config);
        } catch (Exception e) {
            return new Result<VodConfigEntity>().error();
        }
    }
    
    /**
     * 获取视频业务配置
     * @return 配置信息
     */
    @GetMapping("/video")
    public Result<VodConfigEntity> getVideoConfig() {
        try {
            VodConfigEntity config = vodConfigService.getEnabledConfig();
            return new Result<VodConfigEntity>().ok(config);
        } catch (Exception e) {
            return new Result<VodConfigEntity>().error();
        }
    }
    
    /**
     * 保存视频业务配置
     * @param config 配置信息
     * @return 保存结果
     */
    @PostMapping("/video")
    public Result<VideoConfigEntity> saveVideoConfig(@RequestBody VideoConfigEntity config) {
        try {
            // 如果是启用状态，先禁用其他配置
            if (config.getEnable() != null && config.getEnable()) {
                VideoConfigEntity enabledConfig = videoConfigService.getEnabledConfig();
                if (enabledConfig != null && !enabledConfig.getId().equals(config.getId())) {
                    enabledConfig.setEnable(false);
                    videoConfigService.updateById(enabledConfig);
                }
            }
            
            if (config.getId() == null) {
                videoConfigService.save(config);
            } else {
                videoConfigService.updateById(config);
            }

            return new Result<VideoConfigEntity>().ok(config);
        } catch (Exception e) {
            return new Result<VideoConfigEntity>().error();
        }
    }
}