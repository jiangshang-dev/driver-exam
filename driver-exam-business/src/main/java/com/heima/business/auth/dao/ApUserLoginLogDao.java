package com.heima.business.auth.dao;

import com.heima.business.auth.entity.ApUserLoginLogEntity;
import com.heima.common.dao.BaseDao;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户登录日志表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2025-09-25
 */
@Mapper
public interface ApUserLoginLogDao extends BaseDao<ApUserLoginLogEntity> {
	
}