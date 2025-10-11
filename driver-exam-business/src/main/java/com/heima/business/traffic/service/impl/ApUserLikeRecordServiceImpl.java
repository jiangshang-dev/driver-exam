package com.heima.business.traffic.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.heima.business.auth.dao.ApUserDao;
import com.heima.business.auth.entity.ApUserEntity;
import com.heima.business.traffic.dao.ApTrafficVideoDao;
import com.heima.business.traffic.dao.ApUserLikeRecordDao;
import com.heima.business.traffic.dto.ApUserLikeRecordDTO;
import com.heima.business.traffic.entity.ApTrafficVideoEntity;
import com.heima.business.traffic.entity.ApUserLikeRecordEntity;
import com.heima.business.traffic.service.ApUserLikeRecordService;
import com.heima.common.page.PageData;
import com.heima.common.service.impl.CrudServiceImpl;
import cn.hutool.core.util.StrUtil;
import com.heima.common.utils.ConvertUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 用户点赞记录表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2025-10-11
 */
@Service
public class ApUserLikeRecordServiceImpl extends CrudServiceImpl<ApUserLikeRecordDao, ApUserLikeRecordEntity, ApUserLikeRecordDTO> implements ApUserLikeRecordService {

    @Resource
    private ApUserDao apUserDao;

    @Resource
    private ApTrafficVideoDao apTrafficVideoDao;

    @Override
    public QueryWrapper<ApUserLikeRecordEntity> getWrapper(Map<String, Object> params){
        String id = (String)params.get("id");

        QueryWrapper<ApUserLikeRecordEntity> wrapper = new QueryWrapper<>();
        wrapper.eq(StrUtil.isNotBlank(id), "id", id);

        return wrapper;
    }

    @Override
    public PageData<ApUserLikeRecordDTO> pages(Map<String, Object> params) {
        String limitStr = (String) params.get("limit");
        String pageNoStr = (String) params.get("page");

        QueryWrapper<ApUserLikeRecordEntity> wrapper = new QueryWrapper<>();

        // 解析分页参数
        int limit = StrUtil.isBlank(limitStr) ? 10 : Integer.parseInt(limitStr);  // 默认每页10条
        int pageNo = StrUtil.isBlank(pageNoStr) ? 1 : Integer.parseInt(pageNoStr); // 默认第一页

        // 使用 MyBatis Plus 分页查询
        Page<ApUserLikeRecordEntity> page = new Page<>(pageNo, limit);
        page = baseDao.selectPage(page, wrapper);

        List<ApUserLikeRecordEntity> records = page.getRecords();
        records.forEach(record -> {
            ApUserEntity userEntity = apUserDao.selectById(record.getUserId());
            record.setPhone(userEntity.getPhone());
            ApTrafficVideoEntity trafficVideo = apTrafficVideoDao.selectById(record.getTargetId());
            record.setVideoTitle(trafficVideo.getVideoTitle());
        });

        // 转换实体类到 DTO
        List<ApUserLikeRecordDTO> list = ConvertUtils.sourceToTarget(records, ApUserLikeRecordDTO.class);
        return new PageData<>(list, page.getTotal());
    }


}