package com.heima.modules.user.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;

import java.util.Date;

/**
 * 用户表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2025-10-01
 */
@Data
public class ApUserExcel {
    @ExcelProperty(value = "")
    private Long id;
    @ExcelProperty(value = "用户名")
    private String username;
    @ExcelProperty(value = "密码")
    private String password;
    @ExcelProperty(value = "真实姓名")
    private String realName;
    @ExcelProperty(value = "头像")
    private String avatar;
    @ExcelProperty(value = "手机号")
    private String phone;
    @ExcelProperty(value = "邮箱")
    private String email;
    @ExcelProperty(value = "性别 0:未知 1:男 2:女")
    private Integer gender;
    @ExcelProperty(value = "状态 0:禁用 1:正常")
    private Integer status;
    @ExcelProperty(value = "是否为VIP")
    private Integer vipLevel;
    @ExcelProperty(value = "最后登录时间")
    private Date lastLoginTime;
    @ExcelProperty(value = "创建时间")
    private Date createTime;
    @ExcelProperty(value = "更新时间")
    private Date updateTime;

}