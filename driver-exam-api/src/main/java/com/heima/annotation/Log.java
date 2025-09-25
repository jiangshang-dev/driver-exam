package com.heima.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 登录日志注解
 * 用于标记需要记录登录日志的方法
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Log {
    
    /**
     * 登录类型
     * @return 登录类型，如：wechat、phone、sms等
     */
    String type() default "";
    
    /**
     * 操作描述
     * @return 操作描述
     */
    String description() default "";
    
    /**
     * 是否记录请求参数
     * @return true:记录 false:不记录
     */
    boolean recordParams() default false;
    
    /**
     * 是否记录返回结果
     * @return true:记录 false:不记录
     */
    boolean recordResult() default false;

    /**
     * 是否为登录
     * @return true:记录 false:不记录
     */
    boolean isLogin() default false;
}