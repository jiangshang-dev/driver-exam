package com.heima.business.mall.service;

import com.heima.business.mall.entity.ApMallGoodsEntity;
import com.heima.business.mall.entity.ApMallOrderEntity;
import com.heima.business.mall.entity.ApMemberPlanEntity;
import com.heima.business.mall.entity.ApPointsLogEntity;
import com.heima.business.mall.entity.ApPointsRuleEntity;

import java.util.List;
import java.util.Map;

public interface MallService {

    List<ApMallGoodsEntity> listGoods(String goodsType);

    List<ApMemberPlanEntity> listPlans();

    Map<String, Object> pointsSummary(Long userId);

    List<ApPointsLogEntity> pointsLogs(Long userId);

    List<ApPointsRuleEntity> listRules();

    int grant(Long userId, String scene, String bizId, Integer amountFen);

    ApMallOrderEntity createGoodsOrder(Long userId, Long goodsId, String payType, String receiverName, String receiverPhone, String receiverAddress);

    ApMallOrderEntity createVipOrder(Long userId, Long planId);

    List<ApMallOrderEntity> myOrders(Long userId, Integer status);

    ApMallOrderEntity getOwnOrder(Long userId, String orderNo);

    ApMallOrderEntity cancel(Long userId, String orderNo);

    ApMallOrderEntity receive(Long userId, String orderNo);

    ApMallOrderEntity ship(String orderNo, String expressCompany, String expressNo);

    ApMallOrderEntity markPaid(String orderNo);

    boolean allowClientScene(String scene);
}
