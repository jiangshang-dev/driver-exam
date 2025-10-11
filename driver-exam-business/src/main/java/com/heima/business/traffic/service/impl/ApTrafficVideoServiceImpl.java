package com.heima.business.traffic.service.impl;

import cn.hutool.core.util.BooleanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.heima.business.enums.TrafficStatus;
import com.heima.business.traffic.dao.ApTrafficVideoDao;
import com.heima.business.traffic.dao.ApUserLikeRecordDao;
import com.heima.business.traffic.dto.ApTrafficVideoDTO;
import com.heima.business.traffic.entity.ApTrafficVideoEntity;
import com.heima.business.traffic.entity.ApUserLikeRecordEntity;
import com.heima.business.traffic.service.ApTrafficVideoService;
import com.heima.business.traffic.service.ApUserLikeRecordService;
import com.heima.common.page.PageData;
import com.heima.common.redis.RedisUtils;
import com.heima.common.service.impl.CrudServiceImpl;
import cn.hutool.core.util.StrUtil;
import com.heima.common.utils.ConvertUtils;
import com.heima.common.utils.IpUtils;
import com.heima.common.utils.JwtUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
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

    @Resource
    private HttpServletRequest request;

    @Resource
    private ApUserLikeRecordDao apUserLikeRecordDao;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public QueryWrapper<ApTrafficVideoEntity> getWrapper(Map<String, Object> params) {
        String id = (String) params.get("id");
        String videoTitle = (String) params.get("videoTitle");
        String category = (String) params.get("category");

        QueryWrapper<ApTrafficVideoEntity> wrapper = new QueryWrapper<>();
        wrapper.eq(StrUtil.isNotBlank(id), "id", id);
        wrapper.like(StrUtil.isNotBlank(videoTitle), "video_title", videoTitle);
        wrapper.like(StrUtil.isNotBlank(category), "category", category);

        return wrapper;
    }


    @Override
    public PageData<ApTrafficVideoDTO> pageApp(Map<String, Object> params) {
        String id = (String) params.get("id");
        String videoTitle = (String) params.get("videoTitle");
        String category = (String) params.get("category");
        String limitStr = (String) params.get("limit");
        String pageNoStr = (String) params.get("page");

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

    @Override
    public boolean liked(String id, String token) {
        // 1 当前登录用户id
        Long userId = JwtUtils.getUserId(token);
        String ipAddr = IpUtils.getIpAddr(request);

        // 2 判断当前登录用户是否已经点赞
        String key = "video_liked:" + id;
        Boolean isMember = stringRedisTemplate.opsForSet().isMember(key, userId.toString());

        if (BooleanUtil.isFalse(isMember)) {
            // 3 如果未点赞，可以点赞
            // 3.1数据库点赞数+1
            ApTrafficVideoEntity entity = baseDao.selectById(id);
            entity.setLikeCount(entity.getLikeCount() + 1);
            int isSuccess = baseDao.updateById(entity);
            // 3.2保存用户到redis的set集合
            if (isSuccess > 0) {
                stringRedisTemplate.opsForSet().add(key, userId.toString());
            }
            // 存入点赞记录表
            saveUserLikeRecord(userId, Long.parseLong(id), "video", true);
            return true;
        } else {
            // 4.如果已点赞，不能重复点赞，取消点赞
            // 4.1数据库点赞数-1
            ApTrafficVideoEntity entity = baseDao.selectById(id);
            entity.setLikeCount(entity.getLikeCount() - 1);
            int isSuccess = baseDao.updateById(entity);
            // 4.2把用户从redis的set集合中删除
            if (isSuccess > 0) {
                stringRedisTemplate.opsForSet().remove(key, userId.toString());
            }
            // 存入点赞记录表
            saveUserLikeRecord(userId, Long.parseLong(id), "video", false);
            return false;
        }

    }

    @Override
    public boolean videoPlayer(String id, String token) {
        // 当前登录用户id
        Long userId = JwtUtils.getUserId(token);
        String ipAddr = IpUtils.getIpAddr(request);

        ApTrafficVideoEntity entity = baseDao.selectById(id);
        // 当前用户已经播放次数加1一次
        entity.setViewCount(entity.getViewCount() + 1);
        int updated = baseDao.updateById(entity);
        return updated > 0;
    }

    @Override
    public ApTrafficVideoDTO getInfoById(Long id, String token) {
        videoPlayer(id.toString(), token);
        ApTrafficVideoEntity trafficVideo = baseDao.selectById(id);
        isVideoLiked(trafficVideo, token);
        return ConvertUtils.sourceToTarget(trafficVideo, ApTrafficVideoDTO.class);
    }

    private void isVideoLiked(ApTrafficVideoEntity trafficVideo, String token) {
        // 当前登录用户id
        Long userId = JwtUtils.getUserId(token);
        String key = "video_liked:" + trafficVideo.getId();
        Boolean isMember = stringRedisTemplate.opsForSet().isMember(key, userId.toString());
        trafficVideo.setIsLike(BooleanUtil.isTrue(isMember));
    }

    /**
     * 点赞记录表
     */
    private void saveUserLikeRecord(Long userId, Long videoId, String targetType, Boolean isLike) {
        ApUserLikeRecordEntity entity = new ApUserLikeRecordEntity();
        entity.setUserId(userId);
        entity.setTargetId(videoId);
        entity.setTargetType(targetType);
        entity.setIsLike(isLike);
        entity.setCreateTime(new Date());

        ApUserLikeRecordEntity record = apUserLikeRecordDao.getById(userId, videoId, targetType);
        if (record != null) {
            entity.setId(record.getId());
            entity.setUpdateTime(new Date());
            apUserLikeRecordDao.updateById(entity);
        } else {
            entity.setUpdateTime(new Date());
            apUserLikeRecordDao.insert(entity);
        }
    }
}