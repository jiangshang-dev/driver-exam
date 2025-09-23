package com.heima.business.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 第三方登录绑定实体
 */
@Data
@TableName("ap_user_oauth")
public class ApUserOauthEntity {
    
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    /** 用户ID */
    private Long userId;
    
    /** 第三方类型 */
    private String oauthType;
    
    /** 第三方用户ID */
    private String oauthId;
    
    /** 第三方UnionID */
    private String unionId;
    
    /** 第三方用户信息JSON */
    private String oauthInfo;
    
    /** 创建时间 */
    private LocalDateTime createTime;
}