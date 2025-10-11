package com.heima.business.traffic.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.heima.business.traffic.dao.ApUserLikeRecordDao;
import com.heima.business.traffic.dto.ApUserLikeRecordDTO;
import com.heima.business.traffic.entity.ApUserLikeRecordEntity;
import com.heima.business.traffic.service.ApUserLikeRecordService;
import com.heima.common.service.impl.CrudServiceImpl;
import cn.hutool.core.util.StrUtil;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 用户点赞记录表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2025-10-11
 */
@Service
public class ApUserLikeRecordServiceImpl extends CrudServiceImpl<ApUserLikeRecordDao, ApUserLikeRecordEntity, ApUserLikeRecordDTO> implements ApUserLikeRecordService {

    @Override
    public QueryWrapper<ApUserLikeRecordEntity> getWrapper(Map<String, Object> params){
        String id = (String)params.get("id");

        QueryWrapper<ApUserLikeRecordEntity> wrapper = new QueryWrapper<>();
        wrapper.eq(StrUtil.isNotBlank(id), "id", id);

        return wrapper;
    }


}