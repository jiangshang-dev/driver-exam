package com.heima.vod.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.heima.vod.entity.VideoInfoEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 视频信息DAO
 */
@Mapper
public interface VideoInfoDao extends BaseMapper<VideoInfoEntity> {
	
}