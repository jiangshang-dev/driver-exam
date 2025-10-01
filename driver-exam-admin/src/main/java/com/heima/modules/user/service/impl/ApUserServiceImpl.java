package com.heima.modules.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.heima.common.service.impl.CrudServiceImpl;
import cn.hutool.core.util.StrUtil;
import com.heima.modules.user.dao.ApUserDao;
import com.heima.modules.user.dto.ApUserDTO;
import com.heima.modules.user.entity.ApUserEntity;
import com.heima.modules.user.service.ApUserService;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 用户表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2025-10-01
 */
@Service
public class ApUserServiceImpl extends CrudServiceImpl<ApUserDao, ApUserEntity, ApUserDTO> implements ApUserService {

    @Override
    public QueryWrapper<ApUserEntity> getWrapper(Map<String, Object> params){
        String id = (String)params.get("id");

        QueryWrapper<ApUserEntity> wrapper = new QueryWrapper<>();
        wrapper.eq(StrUtil.isNotBlank(id), "id", id);

        return wrapper;
    }


}