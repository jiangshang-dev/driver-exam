package com.heima.business.traffic.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 交规视频表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2025-10-07
 */
@Data
public class ApTrafficVideoExcel {
    @ExcelProperty(value = "视频ID，雪花算法生成")
    private Long id;
    @ExcelProperty(value = "视频标题")
    private String videoTitle;
    @ExcelProperty(value = "视频描述")
    private String videoDesc;
    @ExcelProperty(value = "封面图片URL")
    private String coverImage;
    @ExcelProperty(value = "视频URL")
    private String videoUrl;
    @ExcelProperty(value = "阿里云视频ID")
    private String aliyunVideoId;
    @ExcelProperty(value = "播放凭证")
    private String playAuth;
    @ExcelProperty(value = "视频时长（秒）")
    private Integer videoDuration;
    @ExcelProperty(value = "视频大小（字节）")
    private Long videoSize;
    @ExcelProperty(value = "视频格式")
    private String videoFormat;
    @ExcelProperty(value = "分辨率")
    private String resolution;
    @ExcelProperty(value = "视频分类")
    private String category;
    @ExcelProperty(value = "标签，逗号分隔")
    private String tags;
    @ExcelProperty(value = "讲师名称")
    private String instructor;
    @ExcelProperty(value = "难度等级：1-初级，2-中级，3-高级")
    private Integer difficultyLevel;
    @ExcelProperty(value = "是否VIP视频：0-免费，1-VIP")
    private Integer isVip;
    @ExcelProperty(value = "是否热门：0-否，1-是")
    private Integer isHot;
    @ExcelProperty(value = "是否可下载：0-否，1-是")
    private Integer isDownloadable;
    @ExcelProperty(value = "观看次数")
    private Long viewCount;
    @ExcelProperty(value = "点赞次数")
    private Long likeCount;
    @ExcelProperty(value = "分享次数")
    private Long shareCount;
    @ExcelProperty(value = "评论数")
    private Long commentCount;
    @ExcelProperty(value = "下载次数")
    private Long downloadCount;
    @ExcelProperty(value = "评分(0-5)")
    private BigDecimal rating;
    @ExcelProperty(value = "评分人数")
    private Integer ratingCount;
    @ExcelProperty(value = "排序权重")
    private Integer sortOrder;
    @ExcelProperty(value = "视频状态：0-下架，1-正常，2-审核中，3-转码中")
    private Integer status;
    @ExcelProperty(value = "上传用户ID")
    private Long uploadUserId;
    @ExcelProperty(value = "审核用户ID")
    private Long auditUserId;
    @ExcelProperty(value = "审核时间")
    private Date auditTime;
    @ExcelProperty(value = "发布时间")
    private Date publishTime;
    @ExcelProperty(value = "是否删除：0-未删除，1-已删除")
    private Integer isDeleted;
    @ExcelProperty(value = "创建时间")
    private Date gmtCreate;
    @ExcelProperty(value = "修改时间")
    private Date gmtModified;

}