package com.heima.business.mall.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ap_mall_goods")
public class ApMallGoodsEntity {
    @TableId
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String goodsName;
    private String goodsType;
    private String category;
    private String coverImage;
    private String coverText;
    private String summary;
    private Integer pointsPrice;
    private Integer cashPrice;
    private String rewardType;
    private Integer rewardValue;
    private Integer stock;
    private Integer status;
    private Integer sortOrder;
    private LocalDateTime gmtCreate;
}
