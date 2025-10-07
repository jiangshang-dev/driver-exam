package com.heima.business.traffic.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.heima.business.traffic.dao.ApTrafficSignDao;
import com.heima.business.traffic.dto.ApTrafficSignDTO;
import com.heima.business.traffic.entity.ApTrafficSignEntity;
import com.heima.business.traffic.service.ApTrafficSignService;
import com.heima.common.service.impl.CrudServiceImpl;
import cn.hutool.core.util.StrUtil;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 交通标志表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2025-10-07
 */
@Service
public class ApTrafficSignServiceImpl extends CrudServiceImpl<ApTrafficSignDao, ApTrafficSignEntity, ApTrafficSignDTO> implements ApTrafficSignService {

    @Override
    public QueryWrapper<ApTrafficSignEntity> getWrapper(Map<String, Object> params){
        String id = (String)params.get("id");

        QueryWrapper<ApTrafficSignEntity> wrapper = new QueryWrapper<>();
        wrapper.eq(StrUtil.isNotBlank(id), "id", id);

        return wrapper;
    }


}