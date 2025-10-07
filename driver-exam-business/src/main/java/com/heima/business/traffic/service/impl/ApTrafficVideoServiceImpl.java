package com.heima.business.traffic.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.heima.business.traffic.dao.ApTrafficVideoDao;
import com.heima.business.traffic.dto.ApTrafficVideoDTO;
import com.heima.business.traffic.entity.ApTrafficVideoEntity;
import com.heima.business.traffic.service.ApTrafficVideoService;
import com.heima.common.service.impl.CrudServiceImpl;
import cn.hutool.core.util.StrUtil;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 交规视频表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2025-10-07
 */
@Service
public class ApTrafficVideoServiceImpl extends CrudServiceImpl<ApTrafficVideoDao, ApTrafficVideoEntity, ApTrafficVideoDTO> implements ApTrafficVideoService {

    @Override
    public QueryWrapper<ApTrafficVideoEntity> getWrapper(Map<String, Object> params){
        String id = (String)params.get("id");
        String videoTitle = (String)params.get("videoTitle");

        QueryWrapper<ApTrafficVideoEntity> wrapper = new QueryWrapper<>();
        wrapper.eq(StrUtil.isNotBlank(id), "id", id);
        wrapper.like(StrUtil.isNotBlank(videoTitle), "video_title", videoTitle);

        return wrapper;
    }


}