package com.heima.business.traffic.service;

import java.util.List;

import com.heima.business.traffic.dto.ApVideoCommentDTO;
import com.heima.business.traffic.entity.ApVideoCommentEntity;
import com.heima.common.service.CrudService;

/**
 * 视频评论表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2025-10-12
 */
public interface ApVideoCommentService extends CrudService<ApVideoCommentEntity, ApVideoCommentDTO> {

    /**
     * 根据视频ID获取审核通过的评论列表
     *
     * @param videoId 视频ID
     * @return 审核通过的评论列表
     */
    List<ApVideoCommentDTO> getApprovedCommentsByVideoId(Long videoId, String token);

    /**
     * 回复评论
     *
     * @param parentId 父评论ID
     * @param replyContent 回复内容
     * @param userId 用户ID
     * @return 回复的评论DTO
     */
    ApVideoCommentDTO replyToComment(Long parentId, String replyContent, Long userId);

    boolean liked(String id, String token);

}
