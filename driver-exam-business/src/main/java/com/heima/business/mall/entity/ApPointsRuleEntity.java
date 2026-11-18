package com.heima.business.mall.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ap_points_rule")
public class ApPointsRuleEntity {
    @TableId
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String sceneCode;
    private String sceneName;
    private Integer points;
    private String calcType;
    private Integer dailyLimit;
    private Integer enabled;
    private String remark;
    private LocalDateTime gmtCreate;
}
