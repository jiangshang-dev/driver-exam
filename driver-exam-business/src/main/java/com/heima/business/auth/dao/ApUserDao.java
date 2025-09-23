package com.heima.business.auth.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.heima.business.auth.entity.ApUserEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户数据访问
 */
@Mapper
public interface ApUserDao extends BaseMapper<ApUserEntity> {
}