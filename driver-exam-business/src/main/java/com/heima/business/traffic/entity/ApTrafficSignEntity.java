package com.heima.business.traffic.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 交通标志表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2025-10-07
 */
@Data
@TableName("ap_traffic_sign")
public class ApTrafficSignEntity {

    /**
     * 标志ID，雪花算法生成
     */
	private Long id;
    /**
     * 标志名称
     */
	private String signName;
    /**
     * 标志说明
     */
	private String signDesc;
    /**
     * 标志图片URL
     */
	private String signImage;
    /**
     * 标志类别：warning-警告标志，prohibition-禁令标志等
     */
	private String signCategory;
    /**
     * 标志类型
     */
	private String signType;
    /**
     * 使用场景
     */
	private String usageScene;
    /**
     * 相关法规
     */
	private String relatedRegulations;
    /**
     * 排序权重
     */
	private Integer sortOrder;
    /**
     * 查看次数
     */
	private Long viewCount;
    /**
     * 是否删除：0-未删除，1-已删除
     */
    @TableLogic
	private Integer isDeleted;
    /**
     * 创建时间
     */
	private Date gmtCreate;
    /**
     * 修改时间
     */
	private Date gmtModified;
}