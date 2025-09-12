package com.heima.vod.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.heima.vod.entity.VideoConfigEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 视频业务配置DAO
 */
@Mapper
public interface VideoConfigDao extends BaseMapper<VideoConfigEntity> {
	
}