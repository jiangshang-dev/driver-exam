package com.heima.business.traffic.service.impl;

import java.util.List;
import java.util.Map;

import cn.hutool.core.util.BooleanUtil;
import com.heima.business.auth.dao.ApUserDao;
import com.heima.business.auth.entity.ApUserEntity;
import com.heima.business.traffic.dto.ApTrafficVideoDTO;
import com.heima.business.traffic.entity.ApTrafficVideoEntity;
import com.heima.common.utils.JwtUtils;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.heima.business.traffic.dao.ApVideoCommentDao;
import com.heima.business.traffic.dto.ApVideoCommentDTO;
import com.heima.business.traffic.entity.ApVideoCommentEntity;
import com.heima.business.traffic.service.ApVideoCommentService;
import com.heima.common.service.impl.CrudServiceImpl;
import com.heima.common.utils.ConvertUtils;

import cn.hutool.core.util.StrUtil;

/**
 * 视频评论表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2025-10-12
 */
@Service
public class ApVideoCommentServiceImpl extends CrudServiceImpl<ApVideoCommentDao, ApVideoCommentEntity, ApVideoCommentDTO> implements ApVideoCommentService {

    @Resource
    private ApUserDao apUserDao;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public QueryWrapper<ApVideoCommentEntity> getWrapper(Map<String, Object> params) {
        String id = (String) params.get("id");

        QueryWrapper<ApVideoCommentEntity> wrapper = new QueryWrapper<>();
        wrapper.eq(StrUtil.isNotBlank(id), "id", id);

        return wrapper;
    }

    @Override
    public List<ApVideoCommentDTO> getApprovedCommentsByVideoId(Long videoId, String token) {
        QueryWrapper<ApVideoCommentEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("video_id", videoId);
        wrapper.eq("audit_status", 1); // 1表示审核通过
        wrapper.eq("is_deleted", 0); // 未删除
        wrapper.orderByDesc("gmt_create"); // 按创建时间倒序排列

        List<ApVideoCommentEntity> entities = baseDao.selectList(wrapper);
        List<ApVideoCommentDTO> sourcedToTarget = ConvertUtils.sourceToTarget(entities, ApVideoCommentDTO.class);
        sourcedToTarget.forEach(item -> {
            ApUserEntity apUser = apUserDao.selectById(item.getUserId());
            String phone = apUser.getPhone();
            // 手机号只留前三位和后4位，中间用***
            item.setPhone(phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4));
            isVideoCommentLiked(item, token);
        });
        return sourcedToTarget;
    }

    @Override
    public ApVideoCommentDTO replyToComment(Long parentId, String replyContent, Long userId) {
        // 先获取父评论
        ApVideoCommentEntity parentComment = baseDao.selectById(parentId);
        if (parentComment == null) {
            throw new RuntimeException("父评论不存在");
        }

        // 获取用户信息
        ApUserEntity user = apUserDao.selectById(userId);
        String phone = user.getPhone();
        phone = phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);

        // 创建回复评论
        ApVideoCommentEntity reply = new ApVideoCommentEntity();
        reply.setVideoId(parentComment.getVideoId());
        reply.setParentCommentId(parentId);
        reply.setUserId(userId);
        reply.setCommentContent(replyContent);
        reply.setLikeCount(0L);
        reply.setReplyCount(0L);
        reply.setIsHot(0);
        reply.setAuditStatus(0); // 回复评论默认也需要审核
        reply.setIsDeleted(0);
        reply.setPhone(phone);

        // 保存回复评论
        baseDao.insert(reply);

        // 更新父评论的回复数
        parentComment.setReplyCount(parentComment.getReplyCount() + 1);
        baseDao.updateById(parentComment);

        return ConvertUtils.sourceToTarget(reply, ApVideoCommentDTO.class);
    }

    @Override
    public boolean liked(String id, String token) {

        // 1 当前登录用户id
        Long userId = JwtUtils.getUserId(token);
        // String ipAddr = IpUtils.getIpAddr(request);

        // 2 判断当前登录用户是否已经点赞
        String key = "video_comment_liked:" + id;
        Boolean isMember = stringRedisTemplate.opsForSet().isMember(key, userId.toString());

        if (BooleanUtil.isFalse(isMember)) {
            // 3 如果未点赞，可以点赞
            // 3.1数据库点赞数+1
            ApVideoCommentEntity entity = baseDao.selectById(id);
            entity.setLikeCount(entity.getLikeCount() + 1);
            int isSuccess = baseDao.updateById(entity);
            // 3.2保存用户到redis的set集合
            if (isSuccess > 0) {
                stringRedisTemplate.opsForSet().add(key, userId.toString());
            }
            return true;
        } else {
            // 4.如果已点赞，不能重复点赞，取消点赞
            // 4.1数据库点赞数-1
            ApVideoCommentEntity entity = baseDao.selectById(id);
            entity.setLikeCount(entity.getLikeCount() - 1);
            int isSuccess = baseDao.updateById(entity);
            // 4.2把用户从redis的set集合中删除
            if (isSuccess > 0) {
                stringRedisTemplate.opsForSet().remove(key, userId.toString());
            }
            return false;
        }
    }

    private void isVideoCommentLiked(ApVideoCommentDTO videoComment, String token) {
        // 当前登录用户id
        Long userId = JwtUtils.getUserId(token);
        String key = "video_comment_liked:" + videoComment.getId();
        Boolean isMember = stringRedisTemplate.opsForSet().isMember(key, userId.toString());
        videoComment.setIsLikeComment(BooleanUtil.isTrue(isMember));
    }
}
