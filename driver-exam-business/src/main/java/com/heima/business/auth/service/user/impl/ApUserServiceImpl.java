package com.heima.business.auth.service.user.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.heima.business.auth.dao.ApUserDao;
import com.heima.business.auth.dto.ApUserDTO;
import com.heima.business.auth.entity.ApUserEntity;
import com.heima.business.auth.service.user.ApUserService;
import com.heima.business.traffic.dto.ApTrafficVideoDTO;
import com.heima.business.traffic.entity.ApTrafficVideoEntity;
import com.heima.common.page.PageData;
import com.heima.common.service.impl.CrudServiceImpl;
import com.heima.common.utils.ConvertUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 用户服务
 */
@Slf4j
@Service
public class ApUserServiceImpl extends CrudServiceImpl<ApUserDao, ApUserEntity, ApUserDTO> implements ApUserService {
    
    @Resource
    private ApUserDao authUserDao;

    @Override
    public QueryWrapper<ApUserEntity> getWrapper(Map<String, Object> params){
        String id = (String)params.get("id");

        QueryWrapper<ApUserEntity> wrapper = new QueryWrapper<>();
        wrapper.eq(StrUtil.isNotBlank(id), "id", id);

        return wrapper;
    }

    @Override
    public PageData<ApUserDTO> pages(Map<String, Object> params) {
        String limitStr = (String) params.get("limit");
        String pageNoStr = (String) params.get("page");
        QueryWrapper<ApUserEntity> wrapper = new QueryWrapper<>();
        // 解析分页参数
        int limit = StrUtil.isBlank(limitStr) ? 10 : Integer.parseInt(limitStr);  // 默认每页10条
        int pageNo = StrUtil.isBlank(pageNoStr) ? 1 : Integer.parseInt(pageNoStr); // 默认第一页

        // 使用 MyBatis Plus 分页查询
        Page<ApUserEntity> page = new Page<>(pageNo, limit);
        page = baseDao.selectPage(page, wrapper);

        // 转换实体类到 DTO
        List<ApUserDTO> list = ConvertUtils.sourceToTarget(page.getRecords(), ApUserDTO.class);
        return new PageData<>(list, page.getTotal());
    }

    /**
     * 保存用户
     */
    @Override
    public void save(ApUserEntity user) {
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        authUserDao.insert(user);
    }
    
    /**
     * 更新用户
     *
     * @return
     */
    @Override
    public boolean updateById(ApUserEntity user) {
        user.setUpdateTime(LocalDateTime.now());
        authUserDao.updateById(user);
        return false;
    }
    
    /**
     * 根据ID获取用户
     */
    @Override
    public ApUserEntity getById(Long id) {
        return authUserDao.selectById(id);
    }
    
    /**
     * 更新最后登录时间
     */
    @Override
    public void updateLastLoginTime(Long userId) {
        ApUserEntity user = new ApUserEntity();
        user.setId(userId);
        user.setLastLoginTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        authUserDao.updateById(user);
    }
    
    /**
     * 根据手机号查找用户
     */
    @Override
    public ApUserEntity findByPhone(String phone) {
        return authUserDao.selectOne(new LambdaQueryWrapper<ApUserEntity>()
                .eq(ApUserEntity::getPhone, phone)
                .eq(ApUserEntity::getStatus, 1));
    }

    /**
     * 根据用户名查找用户
     */
    @Override
    public ApUserEntity findByUsername(String username) {
        return authUserDao.selectOne(new LambdaQueryWrapper<ApUserEntity>()
                .eq(ApUserEntity::getUsername, username)
                .eq(ApUserEntity::getStatus, 1));
    }
}