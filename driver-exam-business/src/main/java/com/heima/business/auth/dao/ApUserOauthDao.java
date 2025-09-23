package com.heima.business.auth.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.heima.business.auth.entity.ApUserOauthEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 第三方登录绑定数据访问
 */
@Mapper
public interface ApUserOauthDao extends BaseMapper<ApUserOauthEntity> {
}