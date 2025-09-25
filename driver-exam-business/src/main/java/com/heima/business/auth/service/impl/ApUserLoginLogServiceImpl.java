package com.heima.business.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.heima.business.auth.dao.ApUserLoginLogDao;
import com.heima.business.auth.entity.ApUserLoginLogEntity;
import com.heima.business.auth.service.ApUserLoginLogService;
import com.heima.common.service.impl.CrudServiceImpl;
import cn.hutool.core.util.StrUtil;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 用户登录日志表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2025-09-25
 */
@Service
public class ApUserLoginLogServiceImpl extends CrudServiceImpl<ApUserLoginLogDao, ApUserLoginLogEntity, ApUserLoginLogEntity> implements ApUserLoginLogService {

    @Override
    public QueryWrapper<ApUserLoginLogEntity> getWrapper(Map<String, Object> params){
        String id = (String)params.get("id");

        QueryWrapper<ApUserLoginLogEntity> wrapper = new QueryWrapper<>();
        wrapper.eq(StrUtil.isNotBlank(id), "id", id);

        return wrapper;
    }


}