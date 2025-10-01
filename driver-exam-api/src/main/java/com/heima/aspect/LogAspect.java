package com.heima.aspect;

import java.time.LocalDateTime;
import java.util.Map;

import com.heima.business.auth.entity.ApUserLoginLogEntity;
import com.heima.business.auth.service.user.ApUserLoginLogService;
import jakarta.annotation.Resource;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.heima.annotation.Log;
import com.heima.common.utils.Result;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * 登录日志切面
 * 自动记录带有@LoginLog注解的方法的登录日志
 */
@Aspect
@Component
@Slf4j
public class LogAspect {

    @Resource
    private ApUserLoginLogService userLoginLogService;

    /**
     * 切点：所有带有@LoginLog注解的方法
     */
    @Pointcut("@annotation(com.heima.annotation.Log)")
    public void loginLogPointcut() {}

    /**
     * 方法正常返回后记录成功日志
     */
    @AfterReturning(pointcut = "loginLogPointcut() && @annotation(loginLog)", returning = "result")
    public void logSuccess(JoinPoint joinPoint, Log loginLog, Object result) {
        try {
            // 获取用户ID
            Long userId = extractUserId(result);
            
            // 记录成功日志
            recordLoginLog(joinPoint, loginLog, userId, true, null, result);
            
        } catch (Exception e) {
            log.error("记录登录成功日志失败", e);
        }
    }

    /**
     * 方法抛出异常后记录失败日志
     */
    @AfterThrowing(pointcut = "loginLogPointcut() && @annotation(loginLog)", throwing = "exception")
    public void logFailure(JoinPoint joinPoint, Log loginLog, Exception exception) {
        try {
            // 记录失败日志
            recordLoginLog(joinPoint, loginLog, null, false, exception.getMessage(), null);
            
        } catch (Exception e) {
            log.error("记录登录失败日志失败", e);
        }
    }

    /**
     * 异步记录登录日志
     */
    @Async
    public void recordLoginLog(JoinPoint joinPoint, Log loginLog, Long userId,
                               boolean loginSuccess, String failReason, Object result) {
        try {
            // 获取HTTP请求
            HttpServletRequest request = getHttpServletRequest();
            if (request == null) {
                log.warn("无法获取HttpServletRequest，跳过登录日志记录");
                return;
            }

            // 获取请求信息
            String ip = getClientIpAddress(request);
            String userAgent = request.getHeader("User-Agent");
            String platform = extractPlatform(userAgent);
            String deviceType = extractDeviceType(userAgent);
            LocalDateTime loginTime = LocalDateTime.now();

            // 获取小程序/app端操作系统名称
            String osName = extractOSType(userAgent);
            // 获取操作系统版本
            String osVersion = "";
            
            // 获取登录类型
            String loginType = loginLog.type();
            if (loginType.isEmpty()) {
                loginType = extractLoginTypeFromMethod(joinPoint.getSignature().getName());
            }
            
            // 获取操作描述
            String description = loginLog.description();
            if (description.isEmpty()) {
                description = joinPoint.getSignature().getName();
            }

            // 记录详细日志
            log.info("=== 登录日志记录 ===");
            log.info("用户ID: {}", userId);
            log.info("登录类型: {}", loginType);
            log.info("操作描述: {}", description);
            log.info("登录状态: {}", loginSuccess ? "成功" : "失败");
            log.info("失败原因: {}", failReason);
            log.info("客户端IP: {}", ip);
            log.info("平台类型: {}", platform);
            log.info("设备类型: {}", deviceType);
            log.info("User-Agent: {}", userAgent);
            log.info("登录时间: {}", loginTime);
            
            // 如果需要记录参数
            if (loginLog.recordParams()) {
                Object[] args = joinPoint.getArgs();
                log.info("请求参数: {}", args);
            }
            
            // 如果需要记录返回结果
            if (loginLog.recordResult() && result != null) {
                log.info("返回结果: {}", result);
            }
            
            log.info("=== 登录日志记录结束 ===");

            // TODO: 这里可以保存到数据库
            // loginLogService.saveLoginLog(loginLogEntity);
            boolean login = loginLog.isLogin();
            if (login) {
                ApUserLoginLogEntity loginLogEntity = new ApUserLoginLogEntity();
                loginLogEntity.setUserId(userId);
                loginLogEntity.setLoginTime(loginTime);
                loginLogEntity.setLoginIp(ip);
                loginLogEntity.setIpLocation("");
                loginLogEntity.setDeviceType(deviceType);
                loginLogEntity.setDeviceBrand("");
                loginLogEntity.setDeviceModel("");
                loginLogEntity.setOsName(osName);
                loginLogEntity.setOsVersion("");
                loginLogEntity.setBrowserName("");
                loginLogEntity.setBrowserVersion("");
                loginLogEntity.setPlatform(platform);
                loginLogEntity.setLoginType(loginType);
                loginLogEntity.setUserAgent(userAgent);
                loginLogEntity.setSessionId("");
                loginLogEntity.setLoginStatus(loginSuccess ? 1 : 0);
                loginLogEntity.setFailReason(failReason);
                loginLogEntity.setIsRiskLogin(0);
                loginLogEntity.setRiskLevel(0);
                loginLogEntity.setRiskDesc("");
                loginLogEntity.setDeviceFingerprint("");
                loginLogEntity.setNetworkType("");
                loginLogEntity.setLogoutTime(null);
                loginLogEntity.setOnlineDuration(0);
                loginLogEntity.setClientVersion("");
                loginLogEntity.setCreateTime(loginTime);
                loginLogEntity.setUpdateTime(loginTime);
                userLoginLogService.save(loginLogEntity);
            } else {
                // TODO: 保存其他操作日志
            }
        } catch (Exception e) {
            log.error("记录登录日志异常", e);
        }
    }

    /**
     * 从返回结果中提取用户ID
     */
    private Long extractUserId(Object result) {
        try {
            if (result instanceof Result) {
                Result<?> res = (Result<?>) result;
                Object data = res.getData();
                if (data instanceof Map) {
                    Map<?, ?> map = (Map<?, ?>) data;
                    Object id = map.get("id");
                    if (id instanceof Number) {
                        return ((Number) id).longValue();
                    }
                }
            }
        } catch (Exception e) {
            log.debug("提取用户ID失败", e);
        }
        return null;
    }

    /**
     * 获取HTTP请求对象
     */
    private HttpServletRequest getHttpServletRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            return attributes.getRequest();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取客户端真实IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String[] ipHeaders = {
            "X-Forwarded-For",
            "Proxy-Client-IP", 
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_CLIENT_IP",
            "X-Real-IP"
        };
        
        for (String header : ipHeaders) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        }
        
        return request.getRemoteAddr();
    }

    /**
     * 从User-Agent提取平台信息
     */
    private String extractPlatform(String userAgent) {
        if (userAgent == null) return "unknown";
        
        userAgent = userAgent.toLowerCase();
        if (userAgent.contains("micromessenger")) {
            return "mp-weixin";
        } else if (userAgent.contains("android")) {
            return "app-android";
        } else if (userAgent.contains("iphone") || userAgent.contains("ipad")) {
            return "app-ios";
        } else {
            return "h5";
        }
    }

    /**
     * 从User-Agent提取系统类型
     */
    private String extractOSType(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return "unknown";
        }

        userAgent = userAgent.toLowerCase();

        // iOS 设备（iPhone / iPad）
        if (userAgent.contains("iphone") || userAgent.contains("ipad")) {
            String device = userAgent.contains("iphone") ? "iPhone" : "iPad";
            String iosVersion = "iOS";

            // 尝试提取 iOS 版本：格式如 "cpu iphone os 15_0 like mac os x"
            int osIndex = userAgent.indexOf("cpu ");
            if (osIndex != -1) {
                int likeIndex = userAgent.indexOf(" like mac os x", osIndex);
                if (likeIndex != -1) {
                    String osPart = userAgent.substring(osIndex + 4, likeIndex); // "iphone os 15_0"
                    String[] parts = osPart.split(" ");
                    if (parts.length >= 3 && "os".equals(parts[1])) {
                        String version = parts[2].replace('_', '.'); // "15_0" -> "15.0"
                        iosVersion = "iOS " + version;
                    }
                }
            }

            return iosVersion + " (" + device + ")";
        }

        // Android 设备
        if (userAgent.contains("android")) {
            String androidVersion = "Android";
            String model = "";

            // 提取 Android 版本：格式如 "android 12;"
            int androidIndex = userAgent.indexOf("android ");
            if (androidIndex != -1) {
                int semicolon = userAgent.indexOf(';', androidIndex);
                if (semicolon != -1) {
                    String versionPart = userAgent.substring(androidIndex + 8, semicolon).trim();
                    if (!versionPart.isEmpty()) {
                        androidVersion = "Android " + versionPart;
                    }
                }
            }

            // 尝试提取设备型号：格式如 "Build/SM-G998B" 或直接包含型号
            int buildIndex = userAgent.indexOf("build/");
            if (buildIndex != -1) {
                int spaceAfterBuild = userAgent.indexOf(' ', buildIndex);
                if (spaceAfterBuild == -1) spaceAfterBuild = userAgent.length();
                model = userAgent.substring(buildIndex + 6, spaceAfterBuild).trim();
            } else {
                // 有些 UA 会直接包含设备型号，如 "SM-G998B"，但没有统一规则，这里可选处理
                // 可以根据需求扩展，比如匹配常见品牌关键词
            }

            if (!model.isEmpty()) {
                return androidVersion + " (" + model + ")";
            } else {
                return androidVersion;
            }
        }

        // 其他情况
        return "h5";
    }

    /**
     * 从User-Agent提取设备类型
     */
    private String extractDeviceType(String userAgent) {
        if (userAgent == null) return "unknown";
        
        userAgent = userAgent.toLowerCase();
        if (userAgent.contains("mobile")) {
            return "mobile";
        } else if (userAgent.contains("tablet")) {
            return "tablet";
        } else {
            return "desktop";
        }
    }

    /**
     * 从方法名提取登录类型
     */
    private String extractLoginTypeFromMethod(String methodName) {
        if (methodName.contains("wechat") || methodName.contains("wxMin")) {
            return "wechat";
        } else if (methodName.contains("phone")) {
            return "phone";
        } else if (methodName.contains("sms")) {
            return "sms";
        } else {
            return "unknown";
        }
    }
}