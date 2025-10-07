package com.heima.business.traffic.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;


/**
 * 交通标志表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2025-10-07
 */
@Data
@Schema(name = "交通标志表")
public class ApTrafficSignDTO implements Serializable {
    private static final long serialVersionUID = 1L;

	@SchemaProperty(name = "标志ID，雪花算法生成")
	private Long id;

	@SchemaProperty(name = "标志名称")
	private String signName;

	@SchemaProperty(name = "标志说明")
	private String signDesc;

	@SchemaProperty(name = "标志图片URL")
	private String signImage;

	@SchemaProperty(name = "标志类别：warning-警告标志，prohibition-禁令标志等")
	private String signCategory;

	@SchemaProperty(name = "标志类型")
	private String signType;

	@SchemaProperty(name = "使用场景")
	private String usageScene;

	@SchemaProperty(name = "相关法规")
	private String relatedRegulations;

	@SchemaProperty(name = "排序权重")
	private Integer sortOrder;

	@SchemaProperty(name = "查看次数")
	private Long viewCount;

	@SchemaProperty(name = "是否删除：0-未删除，1-已删除")
	private Integer isDeleted;

	@SchemaProperty(name = "创建时间")
	private Date gmtCreate;

	@SchemaProperty(name = "修改时间")
	private Date gmtModified;


}
