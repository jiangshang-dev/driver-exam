package com.heima.business.mall.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ap_member_plan")
public class ApMemberPlanEntity {
    @TableId
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String planName;
    private Integer days;
    private Integer price;
    private Integer originPrice;
    private Integer pointsReward;
    private Integer recommended;
    private Integer status;
    private Integer sortOrder;
    private LocalDateTime gmtCreate;
}
