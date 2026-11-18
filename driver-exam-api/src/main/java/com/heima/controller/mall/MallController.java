package com.heima.controller.mall;

import com.heima.business.mall.entity.ApMallGoodsEntity;
import com.heima.business.mall.entity.ApMallOrderEntity;
import com.heima.business.mall.entity.ApMemberPlanEntity;
import com.heima.business.mall.entity.ApPointsLogEntity;
import com.heima.business.mall.entity.ApPointsRuleEntity;
import com.heima.business.mall.service.MallService;
import com.heima.common.utils.JwtUtils;
import com.heima.common.utils.Result;
import com.heima.service.WechatPayService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/mall")
public class MallController {

    @Resource
    private MallService mallService;
    @Resource
    private WechatPayService wechatPayService;

    @GetMapping("/goods")
    public Result<List<ApMallGoodsEntity>> goods(@RequestParam(value = "goodsType", required = false) String goodsType) {
        return new Result<List<ApMallGoodsEntity>>().ok(mallService.listGoods(goodsType));
    }

    @GetMapping("/plan")
    public Result<List<ApMemberPlanEntity>> plans() {
        return new Result<List<ApMemberPlanEntity>>().ok(mallService.listPlans());
    }

    @GetMapping("/points/summary")
    public Result<Map<String, Object>> summary(@RequestHeader(value = "Authorization", required = false) String token) {
        Long userId = userId(token);
        if (userId == null) {
            return new Result<Map<String, Object>>().error("请先登录");
        }
        return new Result<Map<String, Object>>().ok(mallService.pointsSummary(userId));
    }

    @GetMapping("/points/logs")
    public Result<List<ApPointsLogEntity>> logs(@RequestHeader(value = "Authorization", required = false) String token) {
        Long userId = userId(token);
        if (userId == null) {
            return new Result<List<ApPointsLogEntity>>().error("请先登录");
        }
        return new Result<List<ApPointsLogEntity>>().ok(mallService.pointsLogs(userId));
    }

    @GetMapping("/points/rules")
    public Result<List<ApPointsRuleEntity>> rules() {
        return new Result<List<ApPointsRuleEntity>>().ok(mallService.listRules());
    }

    @PostMapping("/points/grant")
    public Result<Integer> grant(@RequestHeader(value = "Authorization", required = false) String token,
                                 @RequestBody SceneRequest request) {
        Long userId = userId(token);
        if (userId == null) {
            return new Result<Integer>().error("请先登录");
        }
        if (!mallService.allowClientScene(request.getScene())) {
            return new Result<Integer>().error("这个场景不能由客户端领取");
        }
        int balance = mallService.grant(userId, request.getScene(), java.time.LocalDate.now().toString(), 0);
        return new Result<Integer>().ok(balance);
    }

    @PostMapping("/order")
    public Result<ApMallOrderEntity> createOrder(@RequestHeader(value = "Authorization", required = false) String token,
                                                 @RequestBody OrderRequest request) {
        Long userId = userId(token);
        if (userId == null) {
            return new Result<ApMallOrderEntity>().error("请先登录");
        }
        ApMallOrderEntity order;
        if (request.getPlanId() != null) {
            order = mallService.createVipOrder(userId, request.getPlanId());
        } else {
            order = mallService.createGoodsOrder(userId, request.getGoodsId(), request.getPayType(),
                    request.getReceiverName(), request.getReceiverPhone(), request.getReceiverAddress());
        }
        return new Result<ApMallOrderEntity>().ok(order);
    }

    @GetMapping("/order/mine")
    public Result<List<ApMallOrderEntity>> mine(@RequestHeader(value = "Authorization", required = false) String token,
                                                @RequestParam(value = "status", required = false) Integer status) {
        Long userId = userId(token);
        if (userId == null) {
            return new Result<List<ApMallOrderEntity>>().error("请先登录");
        }
        return new Result<List<ApMallOrderEntity>>().ok(mallService.myOrders(userId, status));
    }

    @GetMapping("/order/{orderNo}")
    public Result<ApMallOrderEntity> order(@RequestHeader(value = "Authorization", required = false) String token,
                                           @PathVariable("orderNo") String orderNo) {
        Long userId = userId(token);
        if (userId == null) {
            return new Result<ApMallOrderEntity>().error("请先登录");
        }
        return new Result<ApMallOrderEntity>().ok(mallService.getOwnOrder(userId, orderNo));
    }

    @PostMapping("/order/cancel")
    public Result<ApMallOrderEntity> cancel(@RequestHeader(value = "Authorization", required = false) String token,
                                            @RequestBody OrderNoRequest request) {
        Long userId = userId(token);
        if (userId == null) {
            return new Result<ApMallOrderEntity>().error("请先登录");
        }
        return new Result<ApMallOrderEntity>().ok(mallService.cancel(userId, request.getOrderNo()));
    }

    @PostMapping("/order/receive")
    public Result<ApMallOrderEntity> receive(@RequestHeader(value = "Authorization", required = false) String token,
                                             @RequestBody OrderNoRequest request) {
        Long userId = userId(token);
        if (userId == null) {
            return new Result<ApMallOrderEntity>().error("请先登录");
        }
        return new Result<ApMallOrderEntity>().ok(mallService.receive(userId, request.getOrderNo()));
    }

    @PostMapping("/order/ship")
    public Result<ApMallOrderEntity> ship(@RequestBody ShipRequest request) {
        if (StringUtils.isAnyBlank(request.getOrderNo(), request.getExpressNo())) {
            return new Result<ApMallOrderEntity>().error("请填写物流单号");
        }
        return new Result<ApMallOrderEntity>().ok(mallService.ship(request.getOrderNo(), request.getExpressCompany(), request.getExpressNo()));
    }

    @PostMapping("/pay/wechat")
    public Result<Map<String, Object>> wechat(@RequestHeader(value = "Authorization", required = false) String token,
                                              @RequestBody OrderNoRequest request) {
        Long userId = userId(token);
        if (userId == null) {
            return new Result<Map<String, Object>>().error("请先登录");
        }
        ApMallOrderEntity order = mallService.getOwnOrder(userId, request.getOrderNo());
        if (order.getStatus() == null || order.getStatus() != 0) {
            return new Result<Map<String, Object>>().error("订单不是待支付状态");
        }
        Map<String, Object> data = new HashMap<>();
        if (!wechatPayService.ready()) {
            data.put("mode", "dev");
            data.put("orderNo", order.getOrderNo());
            return new Result<Map<String, Object>>().ok(data);
        }
        data.put("mode", "jsapi");
        data.put("orderNo", order.getOrderNo());
        data.put("pay", wechatPayService.jsapi(order));
        return new Result<Map<String, Object>>().ok(data);
    }

    @PostMapping("/pay/dev-confirm")
    public Result<ApMallOrderEntity> devConfirm(@RequestHeader(value = "Authorization", required = false) String token,
                                                @RequestBody OrderNoRequest request) {
        if (wechatPayService.ready()) {
            return new Result<ApMallOrderEntity>().error("已配置微信支付，请使用正式支付");
        }
        Long userId = userId(token);
        if (userId == null) {
            return new Result<ApMallOrderEntity>().error("请先登录");
        }
        mallService.getOwnOrder(userId, request.getOrderNo());
        return new Result<ApMallOrderEntity>().ok(mallService.markPaid(request.getOrderNo()));
    }

    @PostMapping(value = "/pay/notify", produces = "application/xml;charset=UTF-8")
    public String notify(HttpServletRequest request) {
        String xml = readBody(request);
        if (!wechatPayService.verifyNotify(xml)) {
            return "<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[SIGN ERROR]]></return_msg></xml>";
        }
        mallService.markPaid(wechatPayService.notifyOrderNo(xml));
        return "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>";
    }

    private String readBody(HttpServletRequest request) {
        StringBuilder body = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                body.append(line);
            }
        } catch (Exception e) {
            return "";
        }
        return body.toString();
    }

    private Long userId(String token) {
        if (StringUtils.isBlank(token)) {
            return null;
        }
        return JwtUtils.getUserId(token);
    }

    @Data
    public static class SceneRequest {
        private String scene;
    }

    @Data
    public static class OrderRequest {
        private Long goodsId;
        private Long planId;
        private String payType;
        private String receiverName;
        private String receiverPhone;
        private String receiverAddress;
    }

    @Data
    public static class OrderNoRequest {
        private String orderNo;
    }

    @Data
    public static class ShipRequest {
        private String orderNo;
        private String expressCompany;
        private String expressNo;
    }
}
