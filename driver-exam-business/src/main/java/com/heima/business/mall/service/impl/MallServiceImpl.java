package com.heima.business.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.heima.business.auth.entity.ApUserEntity;
import com.heima.business.auth.service.user.ApUserService;
import com.heima.business.mall.dao.ApMallGoodsDao;
import com.heima.business.mall.dao.ApMallOrderDao;
import com.heima.business.mall.dao.ApMemberPlanDao;
import com.heima.business.mall.dao.ApPointsLogDao;
import com.heima.business.mall.dao.ApPointsRuleDao;
import com.heima.business.mall.entity.ApMallGoodsEntity;
import com.heima.business.mall.entity.ApMallOrderEntity;
import com.heima.business.mall.entity.ApMemberPlanEntity;
import com.heima.business.mall.entity.ApPointsLogEntity;
import com.heima.business.mall.entity.ApPointsRuleEntity;
import com.heima.business.mall.service.MallService;
import com.heima.common.exception.RenException;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class MallServiceImpl implements MallService {

    private static final Set<String> CLIENT_SCENES = Set.of("MOCK_EXAM", "PRACTICE");

    @Resource
    private ApMallGoodsDao goodsDao;
    @Resource
    private ApMemberPlanDao planDao;
    @Resource
    private ApMallOrderDao orderDao;
    @Resource
    private ApPointsRuleDao ruleDao;
    @Resource
    private ApPointsLogDao logDao;
    @Resource
    private ApUserService userService;

    @Override
    public List<ApMallGoodsEntity> listGoods(String goodsType) {
        QueryWrapper<ApMallGoodsEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1);
        wrapper.eq(StringUtils.isNotBlank(goodsType) && !"ALL".equalsIgnoreCase(goodsType), "goods_type", goodsType);
        wrapper.orderByAsc("sort_order");
        return goodsDao.selectList(wrapper);
    }

    @Override
    public List<ApMemberPlanEntity> listPlans() {
        return planDao.selectList(new QueryWrapper<ApMemberPlanEntity>().eq("status", 1).orderByAsc("sort_order"));
    }

    @Override
    public Map<String, Object> pointsSummary(Long userId) {
        ApUserEntity user = userService.getById(userId);
        int balance = user == null || user.getIntegral() == null ? 0 : user.getIntegral();
        LocalDateTime start = LocalDate.now().atStartOfDay();
        List<ApPointsLogEntity> today = logDao.selectList(new QueryWrapper<ApPointsLogEntity>()
                .eq("user_id", userId).ge("gmt_create", start));
        int todayEarned = 0;
        for (ApPointsLogEntity item : today) {
            if (item.getPoints() != null && item.getPoints() > 0) {
                todayEarned += item.getPoints();
            }
        }
        List<ApPointsLogEntity> all = logDao.selectList(new QueryWrapper<ApPointsLogEntity>().eq("user_id", userId));
        int earned = 0;
        int spent = 0;
        for (ApPointsLogEntity item : all) {
            int points = item.getPoints() == null ? 0 : item.getPoints();
            if (points > 0) {
                earned += points;
            } else {
                spent += -points;
            }
        }
        Map<String, Object> summary = new HashMap<>();
        summary.put("balance", balance);
        summary.put("todayEarned", todayEarned);
        summary.put("totalEarned", earned);
        summary.put("totalSpent", spent);
        summary.put("vipLevel", user == null ? 0 : user.getVipLevel());
        summary.put("vipExpireTime", user == null ? null : user.getVipExpireTime());
        return summary;
    }

    @Override
    public List<ApPointsLogEntity> pointsLogs(Long userId) {
        return logDao.selectList(new QueryWrapper<ApPointsLogEntity>()
                .eq("user_id", userId).orderByDesc("gmt_create").last("LIMIT 50"));
    }

    @Override
    public List<ApPointsRuleEntity> listRules() {
        return ruleDao.selectList(new QueryWrapper<ApPointsRuleEntity>().eq("enabled", 1).orderByAsc("id"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int grant(Long userId, String scene, String bizId, Integer amountFen) {
        if (userId == null || StringUtils.isBlank(scene)) {
            return 0;
        }
        ApPointsRuleEntity rule = ruleDao.selectOne(new QueryWrapper<ApPointsRuleEntity>()
                .eq("scene_code", scene).eq("enabled", 1).last("LIMIT 1"));
        if (rule == null) {
            return 0;
        }
        String biz = bizId == null ? "" : bizId;
        if (StringUtils.isNotBlank(biz)) {
            Long exists = logDao.selectCount(new QueryWrapper<ApPointsLogEntity>()
                    .eq("user_id", userId).eq("scene_code", scene).eq("biz_id", biz));
            if (exists != null && exists > 0) {
                return 0;
            }
        }
        if (rule.getDailyLimit() != null && rule.getDailyLimit() > 0) {
            Long todayCount = logDao.selectCount(new QueryWrapper<ApPointsLogEntity>()
                    .eq("user_id", userId).eq("scene_code", scene)
                    .ge("gmt_create", LocalDate.now().atStartOfDay())
                    .le("gmt_create", LocalDate.now().atTime(LocalTime.MAX)));
            if (todayCount != null && todayCount >= rule.getDailyLimit()) {
                return 0;
            }
        }
        int points = rule.getPoints() == null ? 0 : rule.getPoints();
        if ("FEN".equalsIgnoreCase(rule.getCalcType())) {
            int fen = amountFen == null ? 0 : amountFen;
            points = fen / 100;
        }
        if (points == 0) {
            return 0;
        }
        int balance = changePoints(userId, points, scene, biz, rule.getSceneName());
        return balance;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApMallOrderEntity createGoodsOrder(Long userId, Long goodsId, String payType, String receiverName, String receiverPhone, String receiverAddress) {
        ApMallGoodsEntity goods = goodsDao.selectById(goodsId);
        if (goods == null || goods.getStatus() == null || goods.getStatus() != 1) {
            throw new RenException("商品不存在或已下架");
        }
        if ("PHYSICAL".equals(goods.getGoodsType()) && (StringUtils.isAnyBlank(receiverName, receiverPhone, receiverAddress))) {
            throw new RenException("实物商品需要填写收货信息");
        }
        decreaseStock(goods.getId());
        ApMallOrderEntity order = baseOrder(userId, "GOODS", goods.getGoodsName(), goods.getGoodsType());
        order.setGoodsId(goods.getId());
        order.setReceiverName(receiverName);
        order.setReceiverPhone(receiverPhone);
        order.setReceiverAddress(receiverAddress);
        fillPay(order, payType, goods.getPointsPrice(), goods.getCashPrice(), userId);
        orderDao.insert(order);
        if ("POINTS".equals(order.getPayType())) {
            return finishPaid(order);
        }
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApMallOrderEntity createVipOrder(Long userId, Long planId) {
        ApMemberPlanEntity plan = planDao.selectById(planId);
        if (plan == null || plan.getStatus() == null || plan.getStatus() != 1) {
            throw new RenException("会员套餐不存在");
        }
        ApMallOrderEntity order = baseOrder(userId, "VIP", plan.getPlanName(), "VIP");
        order.setPlanId(plan.getId());
        order.setPayType("WECHAT");
        order.setPointsCost(0);
        order.setCashAmount(plan.getPrice());
        order.setStatus(0);
        orderDao.insert(order);
        return order;
    }

    @Override
    public List<ApMallOrderEntity> myOrders(Long userId, Integer status) {
        QueryWrapper<ApMallOrderEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq(status != null, "status", status);
        wrapper.orderByDesc("gmt_create");
        return orderDao.selectList(wrapper);
    }

    @Override
    public ApMallOrderEntity getOwnOrder(Long userId, String orderNo) {
        ApMallOrderEntity order = orderDao.selectOne(new QueryWrapper<ApMallOrderEntity>().eq("order_no", orderNo).last("LIMIT 1"));
        if (order == null || !order.getUserId().equals(userId)) {
            throw new RenException("订单不存在");
        }
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApMallOrderEntity cancel(Long userId, String orderNo) {
        ApMallOrderEntity order = getOwnOrder(userId, orderNo);
        if (order.getStatus() == null || order.getStatus() != 0) {
            throw new RenException("当前订单不能取消");
        }
        order.setStatus(4);
        order.setGmtModified(LocalDateTime.now());
        orderDao.updateById(order);
        if (order.getGoodsId() != null) {
            goodsDao.update(null, new UpdateWrapper<ApMallGoodsEntity>().setSql("stock = stock + 1").eq("id", order.getGoodsId()));
        }
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApMallOrderEntity receive(Long userId, String orderNo) {
        ApMallOrderEntity order = getOwnOrder(userId, orderNo);
        if (order.getStatus() == null || order.getStatus() != 2) {
            throw new RenException("还没有发货，不能确认收货");
        }
        order.setStatus(3);
        order.setFinishTime(LocalDateTime.now());
        order.setGmtModified(LocalDateTime.now());
        orderDao.updateById(order);
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApMallOrderEntity ship(String orderNo, String expressCompany, String expressNo) {
        ApMallOrderEntity order = orderDao.selectOne(new QueryWrapper<ApMallOrderEntity>().eq("order_no", orderNo).last("LIMIT 1"));
        if (order == null) {
            throw new RenException("订单不存在");
        }
        if (!"PHYSICAL".equals(order.getGoodsType()) || order.getStatus() == null || order.getStatus() != 1) {
            throw new RenException("只有待发货的实物订单可以发货");
        }
        order.setExpressCompany(expressCompany);
        order.setExpressNo(expressNo);
        order.setStatus(2);
        order.setShipTime(LocalDateTime.now());
        order.setGmtModified(LocalDateTime.now());
        orderDao.updateById(order);
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApMallOrderEntity markPaid(String orderNo) {
        ApMallOrderEntity order = orderDao.selectOne(new QueryWrapper<ApMallOrderEntity>().eq("order_no", orderNo).last("LIMIT 1"));
        if (order == null) {
            throw new RenException("订单不存在");
        }
        if (order.getStatus() != null && order.getStatus() != 0) {
            return order;
        }
        int rows = orderDao.update(null, new UpdateWrapper<ApMallOrderEntity>()
                .set("status", 1)
                .set("pay_time", LocalDateTime.now())
                .set("gmt_modified", LocalDateTime.now())
                .eq("id", order.getId())
                .eq("status", 0));
        if (rows == 0) {
            return orderDao.selectById(order.getId());
        }
        order.setStatus(1);
        order.setPayTime(LocalDateTime.now());
        return finishPaid(order);
    }

    @Override
    public boolean allowClientScene(String scene) {
        return CLIENT_SCENES.contains(scene);
    }

    private ApMallOrderEntity finishPaid(ApMallOrderEntity order) {
        deliver(order);
        if ("WECHAT".equals(order.getPayType())) {
            grant(order.getUserId(), "MALL_PAY", order.getOrderNo(), order.getCashAmount());
        }
        if ("VIP".equals(order.getOrderType())) {
            ApMemberPlanEntity plan = planDao.selectById(order.getPlanId());
            if (plan != null) {
                extendVip(order.getUserId(), plan.getDays());
            }
            grant(order.getUserId(), "VIP_OPEN", order.getOrderNo(), 0);
        }
        int status = "PHYSICAL".equals(order.getGoodsType()) ? 1 : 3;
        order.setStatus(status);
        if (status == 3) {
            order.setFinishTime(LocalDateTime.now());
        }
        order.setGmtModified(LocalDateTime.now());
        orderDao.updateById(order);
        return order;
    }

    private void deliver(ApMallOrderEntity order) {
        if (order.getGoodsId() == null) {
            return;
        }
        ApMallGoodsEntity goods = goodsDao.selectById(order.getGoodsId());
        if (goods != null && "VIP_DAYS".equals(goods.getRewardType()) && goods.getRewardValue() != null) {
            extendVip(order.getUserId(), goods.getRewardValue());
        }
    }

    private void extendVip(Long userId, int days) {
        ApUserEntity user = userService.getById(userId);
        if (user == null || days <= 0) {
            return;
        }
        LocalDateTime base = user.getVipExpireTime();
        LocalDateTime now = LocalDateTime.now();
        if (base == null || base.isBefore(now)) {
            base = now;
        }
        user.setVipLevel(1);
        user.setVipExpireTime(base.plusDays(days));
        userService.updateById(user);
    }

    private void fillPay(ApMallOrderEntity order, String payType, Integer pointsPrice, Integer cashPrice, Long userId) {
        if ("POINTS".equalsIgnoreCase(payType)) {
            int cost = pointsPrice == null ? 0 : pointsPrice;
            if (cost <= 0) {
                throw new RenException("该商品不支持积分支付");
            }
            changePoints(userId, -cost, "MALL_COST", order.getOrderNo(), "积分兑换");
            order.setPayType("POINTS");
            order.setPointsCost(cost);
            order.setCashAmount(0);
            order.setStatus(1);
            order.setPayTime(LocalDateTime.now());
            return;
        }
        int cash = cashPrice == null ? 0 : cashPrice;
        if (cash <= 0) {
            throw new RenException("该商品不支持微信支付");
        }
        order.setPayType("WECHAT");
        order.setPointsCost(0);
        order.setCashAmount(cash);
        order.setStatus(0);
    }

    private ApMallOrderEntity baseOrder(Long userId, String orderType, String name, String goodsType) {
        ApMallOrderEntity order = new ApMallOrderEntity();
        order.setOrderNo("M" + System.currentTimeMillis());
        order.setUserId(userId);
        order.setOrderType(orderType);
        order.setGoodsName(name);
        order.setGoodsType(goodsType);
        order.setGmtCreate(LocalDateTime.now());
        order.setGmtModified(LocalDateTime.now());
        return order;
    }

    private void decreaseStock(Long goodsId) {
        int rows = goodsDao.update(null, new UpdateWrapper<ApMallGoodsEntity>()
                .setSql("stock = stock - 1")
                .eq("id", goodsId)
                .gt("stock", 0));
        if (rows == 0) {
            throw new RenException("库存不足");
        }
    }

    private int changePoints(Long userId, int delta, String scene, String bizId, String remark) {
        ApUserEntity user = userService.getById(userId);
        if (user == null) {
            throw new RenException("用户不存在");
        }
        int balance = (user.getIntegral() == null ? 0 : user.getIntegral()) + delta;
        if (balance < 0) {
            throw new RenException("积分不足");
        }
        user.setIntegral(balance);
        userService.updateById(user);
        ApPointsLogEntity log = new ApPointsLogEntity();
        log.setUserId(userId);
        log.setSceneCode(scene);
        log.setPoints(delta);
        log.setBalance(balance);
        log.setBizId(bizId == null ? "" : bizId);
        log.setRemark(remark);
        log.setGmtCreate(LocalDateTime.now());
        logDao.insert(log);
        return balance;
    }
}
