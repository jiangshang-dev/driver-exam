package com.heima.business.traffic.dao;

import com.heima.business.traffic.entity.ApUserLikeRecordEntity;
import com.heima.common.dao.BaseDao;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户点赞记录表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2025-10-11
 */
@Mapper
public interface ApUserLikeRecordDao extends BaseDao<ApUserLikeRecordEntity> {

    ApUserLikeRecordEntity getById(@Param("userId") Long userId, @Param("videoId") Long videoId, @Param("targetType") String targetType);
}