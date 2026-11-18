package com.heima.business.traffic.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 视频评论审核参数
 */
@Data
@Schema(name = "视频评论审核参数")
public class ApVideoCommentAuditDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(name = "评论ID集合")
    private List<Long> ids;

    @Schema(name = "审核状态：1-通过，2-不通过")
    private Integer auditStatus;

    @Schema(name = "审核备注")
    private String auditRemark;
}
