package com.heima.business.uniad.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.heima.business.auth.dto.LoginResult;
import com.heima.business.auth.entity.ApUserEntity;
import com.heima.business.auth.service.user.ApUserService;
import com.heima.business.enums.TrafficStatus;
import com.heima.business.traffic.dto.ApTrafficVideoDTO;
import com.heima.business.traffic.entity.ApTrafficVideoEntity;
import com.heima.business.uniad.dao.ApAdWatchRecordDao;
import com.heima.business.uniad.dto.ApAdWatchRecordDTO;
import com.heima.business.uniad.entity.ApAdWatchRecordEntity;
import com.heima.business.uniad.service.ApAdWatchRecordService;
import com.heima.common.page.PageData;
import com.heima.common.service.impl.CrudServiceImpl;
import cn.hutool.core.util.StrUtil;
import com.heima.common.utils.ConvertUtils;
import com.heima.common.utils.IpUtils;
import com.heima.common.utils.JwtUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 广告观看记录表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2025-10-08
 */
@Service
public class ApAdWatchRecordServiceImpl extends CrudServiceImpl<ApAdWatchRecordDao, ApAdWatchRecordEntity, ApAdWatchRecordDTO> implements ApAdWatchRecordService {

    @Resource
    private ApUserService apUserService;
    @Resource
    private HttpServletRequest request;

    @Override
    public QueryWrapper<ApAdWatchRecordEntity> getWrapper(Map<String, Object> params) {
        String id = (String) params.get("id");

        QueryWrapper<ApAdWatchRecordEntity> wrapper = new QueryWrapper<>();
        wrapper.eq(StrUtil.isNotBlank(id), "id", id);
        wrapper.orderByDesc("watch_date");

        return wrapper;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginResult adWatchRecord(String token) {
        Long userId = JwtUtils.getUserId(token);
        ApAdWatchRecordEntity record = new ApAdWatchRecordEntity();
        record.setUserId(userId);
        record.setAdId("1");
        record.setAdPlatform("uni-ad");
        record.setAdType("rewarded_video");
        record.setRewardAmount(1);
        record.setRewardStatus(1);
        record.setRewardType("vip_days");
        record.setWatchDate(LocalDateTime.now());
        record.setPlatformType("mp-weixin");
        record.setIpAddress(IpUtils.getIpAddr(request));
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        int insert = baseDao.insert(record);
        ApUserEntity user = apUserService.getById(userId);
        user.setVipLevel(1);
        user.setVipExpireTime(LocalDateTime.now().plusDays(1));
        user.setIntegral(user.getIntegral() + 1);
        apUserService.updateById(user);

        LoginResult result = new LoginResult();
        result.setId(user.getId());
        result.setAccount(user.getUsername());
        result.setNickname(user.getRealName());
        result.setMobile(user.getPhone());
        result.setToken(token);
        result.setVipLevel(user.getVipLevel());
        result.setIntegral(user.getIntegral());
        result.setVipExpireTime(user.getVipExpireTime());
        result.setAvatar(user.getAvatar());
        return result;
    }

    @Override
    public PageData<ApAdWatchRecordDTO> pages(Map<String, Object> params) {

        String id = (String) params.get("id");
        String phone = (String) params.get("phone");
        String limitStr = (String) params.get("limit");
        String pageNoStr = (String) params.get("page");

        QueryWrapper<ApAdWatchRecordEntity> wrapper = new QueryWrapper<>();

        // 解析分页参数
        int limit = StrUtil.isBlank(limitStr) ? 10 : Integer.parseInt(limitStr);  // 默认每页10条
        int pageNo = StrUtil.isBlank(pageNoStr) ? 1 : Integer.parseInt(pageNoStr); // 默认第一页

        // 使用 MyBatis Plus 分页查询
        Page<ApAdWatchRecordEntity> page = new Page<>(pageNo, limit);
        page = baseDao.selectPage(page, wrapper);

        List<ApAdWatchRecordEntity> records = page.getRecords();
        records.forEach(record -> {
            ApUserEntity user = apUserService.getById(record.getUserId());
            record.setUserNickname(StrUtil.isBlank(user.getPhone())? user.getRealName() : user.getPhone());
        });
        records = records.stream().filter(record -> record.getUserNickname().contains(phone)).toList();
        // 转换实体类到 DTO
        List<ApAdWatchRecordDTO> list = ConvertUtils.sourceToTarget(records, ApAdWatchRecordDTO.class);
        return new PageData<>(list, page.getTotal());
    }
}