package com.heima.modules.user.dao;

import com.heima.common.dao.BaseDao;
import com.heima.modules.user.entity.ApUserEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2025-10-01
 */
@Mapper
public interface ApUserDao extends BaseDao<ApUserEntity> {
	
}