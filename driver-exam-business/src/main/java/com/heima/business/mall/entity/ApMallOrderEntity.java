package com.heima.business.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ap_mall_order")
public class ApMallOrderEntity {
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String orderNo;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;
    private String orderType;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long goodsId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long planId;
    private String goodsName;
    private String goodsType;
    private String payType;
    private Integer pointsCost;
    private Integer cashAmount;
    private Integer status;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private String expressCompany;
    private String expressNo;
    private LocalDateTime payTime;
    private LocalDateTime shipTime;
    private LocalDateTime finishTime;
    private LocalDateTime gmtCreate;
    private LocalDateTime gmtModified;
}
