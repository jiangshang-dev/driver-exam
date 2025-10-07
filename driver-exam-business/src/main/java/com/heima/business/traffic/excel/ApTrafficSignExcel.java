package com.heima.business.traffic.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;

import java.util.Date;

/**
 * 交通标志表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2025-10-07
 */
@Data
public class ApTrafficSignExcel {
    @ExcelProperty(value = "标志ID，雪花算法生成")
    private Long id;
    @ExcelProperty(value = "标志名称")
    private String signName;
    @ExcelProperty(value = "标志说明")
    private String signDesc;
    @ExcelProperty(value = "标志图片URL")
    private String signImage;
    @ExcelProperty(value = "标志类别：warning-警告标志，prohibition-禁令标志等")
    private String signCategory;
    @ExcelProperty(value = "标志类型")
    private String signType;
    @ExcelProperty(value = "使用场景")
    private String usageScene;
    @ExcelProperty(value = "相关法规")
    private String relatedRegulations;
    @ExcelProperty(value = "排序权重")
    private Integer sortOrder;
    @ExcelProperty(value = "查看次数")
    private Long viewCount;
    @ExcelProperty(value = "是否删除：0-未删除，1-已删除")
    private Integer isDeleted;
    @ExcelProperty(value = "创建时间")
    private Date gmtCreate;
    @ExcelProperty(value = "修改时间")
    private Date gmtModified;

}