package com.heima.business.traffic.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.heima.business.enums.TrafficStatus;
import com.heima.business.traffic.dao.ApTrafficVideoDao;
import com.heima.business.traffic.dto.ApTrafficVideoDTO;
import com.heima.business.traffic.entity.ApTrafficVideoEntity;
import com.heima.business.traffic.service.ApTrafficVideoService;
import com.heima.common.page.PageData;
import com.heima.common.service.impl.CrudServiceImpl;
import cn.hutool.core.util.StrUtil;
import com.heima.common.utils.ConvertUtils;
import org.springframework.stereotype.Service;

import java.util.List;
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
        String category = (String)params.get("category");

        QueryWrapper<ApTrafficVideoEntity> wrapper = new QueryWrapper<>();
        wrapper.eq(StrUtil.isNotBlank(id), "id", id);
        wrapper.like(StrUtil.isNotBlank(videoTitle), "video_title", videoTitle);
        wrapper.like(StrUtil.isNotBlank(category), "category", category);

        return wrapper;
    }


    @Override
    public PageData<ApTrafficVideoDTO> pageApp(Map<String, Object> params) {
        String id = (String)params.get("id");
        String videoTitle = (String)params.get("videoTitle");
        String category = (String)params.get("category");
        String limitStr = (String)params.get("limit");
        String pageNoStr = (String)params.get("page");

        QueryWrapper<ApTrafficVideoEntity> wrapper = new QueryWrapper<>();
        wrapper.eq(StrUtil.isNotBlank(id), "id", id);
        wrapper.like(StrUtil.isNotBlank(videoTitle), "video_title", videoTitle);
        wrapper.like(StrUtil.isNotBlank(category), "category", category);
        wrapper.eq("status", TrafficStatus.NORMAL.getValue());

        // 解析分页参数
        int limit = StrUtil.isBlank(limitStr) ? 10 : Integer.parseInt(limitStr);  // 默认每页10条
        int pageNo = StrUtil.isBlank(pageNoStr) ? 1 : Integer.parseInt(pageNoStr); // 默认第一页


        // 使用 MyBatis Plus 分页查询
        Page<ApTrafficVideoEntity> page = new Page<>(pageNo, limit);
        page = baseDao.selectPage(page, wrapper);

        // 转换实体类到 DTO
        List<ApTrafficVideoDTO> list = ConvertUtils.sourceToTarget(page.getRecords(), ApTrafficVideoDTO.class);
        return new PageData<>(list, page.getTotal());
    }
}