package com.heima.business.traffic.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.heima.business.traffic.dao.ApTrafficSignDao;
import com.heima.business.traffic.dto.ApTrafficSignDTO;
import com.heima.business.traffic.entity.ApTrafficSignEntity;
import com.heima.business.traffic.service.ApTrafficSignService;
import com.heima.common.page.PageData;
import com.heima.common.service.impl.CrudServiceImpl;
import cn.hutool.core.util.StrUtil;
import com.heima.common.utils.ConvertUtils;
import org.springframework.stereotype.Service;

import java.util.List;
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
        String signName = (String)params.get("signName");
        String signCategory = (String)params.get("signCategory");

        QueryWrapper<ApTrafficSignEntity> wrapper = new QueryWrapper<>();
        wrapper.eq(StrUtil.isNotBlank(id), "id", id);
        wrapper.like(StrUtil.isNotBlank(signName), "sign_name", signName);
        wrapper.like(StrUtil.isNotBlank(signCategory), "sign_category", signCategory);

        return wrapper;
    }


    @Override
    public PageData<ApTrafficSignDTO> pageApp(Map<String, Object> params) {
        String limitStr = (String)params.get("limit");
        String pageNoStr = (String)params.get("page");

        String id = (String)params.get("id");
        String signName = (String)params.get("signName");
        String signCategory = (String)params.get("signCategory");

        QueryWrapper<ApTrafficSignEntity> wrapper = new QueryWrapper<>();
        wrapper.eq(StrUtil.isNotBlank(id), "id", id);
        wrapper.like(StrUtil.isNotBlank(signName), "sign_name", signName);
        wrapper.like(StrUtil.isNotBlank(signCategory), "sign_category", signCategory);

        // 解析分页参数
        int limit = StrUtil.isBlank(limitStr) ? 10 : Integer.parseInt(limitStr);  // 默认每页10条
        int pageNo = StrUtil.isBlank(pageNoStr) ? 1 : Integer.parseInt(pageNoStr); // 默认第一页


        // 使用 MyBatis Plus 分页查询
        Page<ApTrafficSignEntity> page = new Page<>(pageNo, limit);
        page = baseDao.selectPage(page, wrapper);

        // 转换实体类到 DTO
        List<ApTrafficSignDTO> list = ConvertUtils.sourceToTarget(page.getRecords(), ApTrafficSignDTO.class);
        return new PageData<>(list, page.getTotal());
    }
}