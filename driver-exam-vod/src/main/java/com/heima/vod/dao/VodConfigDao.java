package com.heima.vod.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.heima.vod.entity.VodConfigEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 阿里云VOD配置DAO
 */
@Mapper
public interface VodConfigDao extends BaseMapper<VodConfigEntity> {
	
}