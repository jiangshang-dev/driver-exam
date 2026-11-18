-- 积分、会员、商城订单。执行前请确认库名为 drive。

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS ap_points_rule (
    id          BIGINT       NOT NULL COMMENT '主键',
    scene_code  VARCHAR(32)  NOT NULL COMMENT '场景编码',
    scene_name  VARCHAR(64)  NOT NULL COMMENT '场景名称',
    points      INT          NOT NULL DEFAULT 0 COMMENT '固定积分，calc_type=FEN 时忽略',
    calc_type   VARCHAR(16)  NOT NULL DEFAULT 'FIXED' COMMENT 'FIXED 固定积分，FEN 按实付金额每元1积分',
    daily_limit INT          NOT NULL DEFAULT 0 COMMENT '每天最多发放次数，0 表示不按天限制',
    enabled     TINYINT      NOT NULL DEFAULT 1 COMMENT '1启用',
    remark      VARCHAR(255)          DEFAULT NULL,
    gmt_create  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_points_scene (scene_code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='积分发放规则';

CREATE TABLE IF NOT EXISTS ap_points_log (
    id         BIGINT      NOT NULL,
    user_id    BIGINT      NOT NULL,
    scene_code VARCHAR(32) NOT NULL,
    points     INT         NOT NULL COMMENT '正数为获得，负数为消耗',
    balance    INT         NOT NULL COMMENT '变动后余额',
    biz_id     VARCHAR(64) NOT NULL DEFAULT '' COMMENT '业务单号，用于去重',
    remark     VARCHAR(255)         DEFAULT NULL,
    gmt_create DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_points_user (user_id, gmt_create),
    KEY idx_points_biz (user_id, scene_code, biz_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='积分流水';

CREATE TABLE IF NOT EXISTS ap_member_plan (
    id            BIGINT      NOT NULL,
    plan_name     VARCHAR(32) NOT NULL,
    days          INT         NOT NULL,
    price         INT         NOT NULL COMMENT '售价，单位分',
    origin_price  INT         NOT NULL DEFAULT 0 COMMENT '原价，单位分',
    points_reward INT         NOT NULL DEFAULT 0 COMMENT '开通成功赠送积分',
    recommended   TINYINT     NOT NULL DEFAULT 0,
    status        TINYINT     NOT NULL DEFAULT 1,
    sort_order    INT         NOT NULL DEFAULT 0,
    gmt_create    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='会员套餐';

CREATE TABLE IF NOT EXISTS ap_mall_goods (
    id            BIGINT       NOT NULL,
    goods_name    VARCHAR(64)  NOT NULL,
    goods_type    VARCHAR(16)  NOT NULL COMMENT 'VIRTUAL 虚拟，PHYSICAL 实物',
    category      VARCHAR(32)  NOT NULL,
    cover_image   VARCHAR(255)          DEFAULT NULL COMMENT '封面图片URL',
    cover_text    VARCHAR(8)            DEFAULT NULL,
    summary       VARCHAR(255)          DEFAULT NULL,
    points_price  INT          NOT NULL DEFAULT 0 COMMENT '积分价',
    cash_price    INT          NOT NULL DEFAULT 0 COMMENT '现金价，单位分',
    reward_type   VARCHAR(16)  NOT NULL DEFAULT 'NONE' COMMENT 'NONE 或 VIP_DAYS',
    reward_value  INT          NOT NULL DEFAULT 0,
    stock         INT          NOT NULL DEFAULT 0,
    status        TINYINT      NOT NULL DEFAULT 1,
    sort_order    INT          NOT NULL DEFAULT 0,
    gmt_create    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_goods_type (goods_type, status, sort_order)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='积分商城商品';

CREATE TABLE IF NOT EXISTS ap_mall_order (
    id               BIGINT       NOT NULL,
    order_no         VARCHAR(32)  NOT NULL,
    user_id          BIGINT       NOT NULL,
    order_type       VARCHAR(16)  NOT NULL COMMENT 'GOODS 商品，VIP 会员',
    goods_id         BIGINT                DEFAULT NULL,
    plan_id          BIGINT                DEFAULT NULL,
    goods_name       VARCHAR(64)  NOT NULL,
    goods_type       VARCHAR(16)  NOT NULL COMMENT 'VIRTUAL PHYSICAL VIP',
    pay_type         VARCHAR(16)  NOT NULL COMMENT 'POINTS 积分，WECHAT 微信支付',
    points_cost      INT          NOT NULL DEFAULT 0,
    cash_amount      INT          NOT NULL DEFAULT 0 COMMENT '单位分',
    status           TINYINT      NOT NULL DEFAULT 0 COMMENT '0待支付 1待发货 2已发货 3已完成 4已取消',
    receiver_name    VARCHAR(32)           DEFAULT NULL,
    receiver_phone   VARCHAR(20)           DEFAULT NULL,
    receiver_address VARCHAR(255)          DEFAULT NULL,
    express_company  VARCHAR(32)           DEFAULT NULL,
    express_no       VARCHAR(64)           DEFAULT NULL,
    pay_time         DATETIME              DEFAULT NULL,
    ship_time        DATETIME              DEFAULT NULL,
    finish_time      DATETIME              DEFAULT NULL,
    gmt_create       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    gmt_modified     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_mall_order_no (order_no),
    KEY idx_mall_order_user (user_id, status, gmt_create)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='商城与会员订单';

INSERT INTO ap_points_rule (id, scene_code, scene_name, points, calc_type, daily_limit, remark)
SELECT 8811000000000000001, 'CHECKIN', '每日签到', 5, 'FIXED', 1, '每天第一次打卡' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ap_points_rule WHERE scene_code = 'CHECKIN');
INSERT INTO ap_points_rule (id, scene_code, scene_name, points, calc_type, daily_limit, remark)
SELECT 8811000000000000002, 'MOCK_EXAM', '完成模拟考试', 8, 'FIXED', 1, '每天第一次交卷' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ap_points_rule WHERE scene_code = 'MOCK_EXAM');
INSERT INTO ap_points_rule (id, scene_code, scene_name, points, calc_type, daily_limit, remark)
SELECT 8811000000000000003, 'PRACTICE', '顺序刷题', 3, 'FIXED', 1, '每天第一次进入刷题结果或主动领取' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ap_points_rule WHERE scene_code = 'PRACTICE');
INSERT INTO ap_points_rule (id, scene_code, scene_name, points, calc_type, daily_limit, remark)
SELECT 8811000000000000004, 'VIP_OPEN', '开通会员', 100, 'FIXED', 0, '每笔会员订单支付成功' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ap_points_rule WHERE scene_code = 'VIP_OPEN');
INSERT INTO ap_points_rule (id, scene_code, scene_name, points, calc_type, daily_limit, remark)
SELECT 8811000000000000005, 'MALL_PAY', '商城现金下单', 0, 'FEN', 0, '微信支付成功后按实付金额每元赠送1积分' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ap_points_rule WHERE scene_code = 'MALL_PAY');

INSERT INTO ap_member_plan (id, plan_name, days, price, origin_price, points_reward, recommended, sort_order)
SELECT 8812000000000000001, '月卡', 30, 1990, 2990, 100, 0, 10 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ap_member_plan WHERE id = 8812000000000000001);
INSERT INTO ap_member_plan (id, plan_name, days, price, origin_price, points_reward, recommended, sort_order)
SELECT 8812000000000000002, '季卡', 90, 4990, 8970, 100, 1, 20 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ap_member_plan WHERE id = 8812000000000000002);
INSERT INTO ap_member_plan (id, plan_name, days, price, origin_price, points_reward, recommended, sort_order)
SELECT 8812000000000000003, '年卡', 365, 12800, 23880, 100, 0, 30 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ap_member_plan WHERE id = 8812000000000000003);

INSERT INTO ap_mall_goods (id, goods_name, goods_type, category, cover_text, summary, points_price, cash_price, reward_type, reward_value, stock, sort_order)
SELECT 8813000000000000001, '7天VIP体验', 'VIRTUAL', '会员', 'VIP', '支付成功后会员有效期增加7天', 80, 990, 'VIP_DAYS', 7, 9999, 10 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ap_mall_goods WHERE id = 8813000000000000001);
INSERT INTO ap_mall_goods (id, goods_name, goods_type, category, cover_text, summary, points_price, cash_price, reward_type, reward_value, stock, sort_order)
SELECT 8813000000000000002, '易错题电子讲义', 'VIRTUAL', '资料', '讲义', '虚拟资料，支付后即可在订单中查看', 30, 600, 'NONE', 0, 9999, 20 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ap_mall_goods WHERE id = 8813000000000000002);
INSERT INTO ap_mall_goods (id, goods_name, goods_type, category, cover_text, summary, points_price, cash_price, reward_type, reward_value, stock, sort_order)
SELECT 8813000000000000003, '科目一速记卡', 'PHYSICAL', '教辅', '速记', '实物卡片，支付后等待发货', 120, 1990, 'NONE', 0, 200, 30 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ap_mall_goods WHERE id = 8813000000000000003);
INSERT INTO ap_mall_goods (id, goods_name, goods_type, category, cover_text, summary, points_price, cash_price, reward_type, reward_value, stock, sort_order)
SELECT 8813000000000000004, '驾考练习握力球', 'PHYSICAL', '周边', '握力', '实物周边，需要填写收货地址', 200, 2990, 'NONE', 0, 100, 40 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ap_mall_goods WHERE id = 8813000000000000004);

-- 积分商城后台菜单（父菜单：积分商城 1973334844665090050）
INSERT INTO sys_menu (id, pid, name, url, permissions, menu_type, icon, sort, creator, create_date, updater, update_date)
SELECT 1978000000000000101, 1973334844665090050, '商品管理', '/mall/goods', '', 0, 'icon-appstore-fill', 0, 1067246875800000001, NOW(), 1067246875800000001, NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 1978000000000000101);
INSERT INTO sys_menu (id, pid, name, url, permissions, menu_type, icon, sort, creator, create_date, updater, update_date)
SELECT 1978000000000000102, 1973334844665090050, '会员套餐', '/mall/plan', '', 0, 'icon-golden-fill', 1, 1067246875800000001, NOW(), 1067246875800000001, NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 1978000000000000102);
INSERT INTO sys_menu (id, pid, name, url, permissions, menu_type, icon, sort, creator, create_date, updater, update_date)
SELECT 1978000000000000103, 1973334844665090050, '积分规则', '/mall/pointsRule', '', 0, 'icon-setting', 2, 1067246875800000001, NOW(), 1067246875800000001, NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 1978000000000000103);
INSERT INTO sys_menu (id, pid, name, url, permissions, menu_type, icon, sort, creator, create_date, updater, update_date)
SELECT 1978000000000000104, 1973334844665090050, '积分记录', '/mall/pointsLog', '', 0, 'icon-unorderedlist', 3, 1067246875800000001, NOW(), 1067246875800000001, NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 1978000000000000104);
INSERT INTO sys_menu (id, pid, name, url, permissions, menu_type, icon, sort, creator, create_date, updater, update_date)
SELECT 1978000000000000105, 1973334844665090050, '订单管理', '/mall/order', '', 0, 'icon-carryout-fill', 4, 1067246875800000001, NOW(), 1067246875800000001, NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 1978000000000000105);

INSERT INTO sys_menu (id, pid, name, url, permissions, menu_type, icon, sort, creator, create_date, updater, update_date)
SELECT 1978000000000000201, 1978000000000000101, '查看', '', 'mall:goods:page,mall:goods:info', 1, '', 0, 1067246875800000001, NOW(), 1067246875800000001, NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 1978000000000000201);
INSERT INTO sys_menu (id, pid, name, url, permissions, menu_type, icon, sort, creator, create_date, updater, update_date)
SELECT 1978000000000000202, 1978000000000000101, '保存', '', 'mall:goods:save', 1, '', 1, 1067246875800000001, NOW(), 1067246875800000001, NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 1978000000000000202);
INSERT INTO sys_menu (id, pid, name, url, permissions, menu_type, icon, sort, creator, create_date, updater, update_date)
SELECT 1978000000000000203, 1978000000000000101, '修改', '', 'mall:goods:update', 1, '', 2, 1067246875800000001, NOW(), 1067246875800000001, NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 1978000000000000203);
INSERT INTO sys_menu (id, pid, name, url, permissions, menu_type, icon, sort, creator, create_date, updater, update_date)
SELECT 1978000000000000204, 1978000000000000101, '删除', '', 'mall:goods:delete', 1, '', 3, 1067246875800000001, NOW(), 1067246875800000001, NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 1978000000000000204);

INSERT INTO sys_menu (id, pid, name, url, permissions, menu_type, icon, sort, creator, create_date, updater, update_date)
SELECT 1978000000000000211, 1978000000000000102, '查看', '', 'mall:plan:page,mall:plan:info', 1, '', 0, 1067246875800000001, NOW(), 1067246875800000001, NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 1978000000000000211);
INSERT INTO sys_menu (id, pid, name, url, permissions, menu_type, icon, sort, creator, create_date, updater, update_date)
SELECT 1978000000000000212, 1978000000000000102, '保存', '', 'mall:plan:save', 1, '', 1, 1067246875800000001, NOW(), 1067246875800000001, NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 1978000000000000212);
INSERT INTO sys_menu (id, pid, name, url, permissions, menu_type, icon, sort, creator, create_date, updater, update_date)
SELECT 1978000000000000213, 1978000000000000102, '修改', '', 'mall:plan:update', 1, '', 2, 1067246875800000001, NOW(), 1067246875800000001, NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 1978000000000000213);
INSERT INTO sys_menu (id, pid, name, url, permissions, menu_type, icon, sort, creator, create_date, updater, update_date)
SELECT 1978000000000000214, 1978000000000000102, '删除', '', 'mall:plan:delete', 1, '', 3, 1067246875800000001, NOW(), 1067246875800000001, NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 1978000000000000214);

INSERT INTO sys_menu (id, pid, name, url, permissions, menu_type, icon, sort, creator, create_date, updater, update_date)
SELECT 1978000000000000221, 1978000000000000103, '查看', '', 'mall:pointsrule:page,mall:pointsrule:info', 1, '', 0, 1067246875800000001, NOW(), 1067246875800000001, NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 1978000000000000221);
INSERT INTO sys_menu (id, pid, name, url, permissions, menu_type, icon, sort, creator, create_date, updater, update_date)
SELECT 1978000000000000222, 1978000000000000103, '保存', '', 'mall:pointsrule:save', 1, '', 1, 1067246875800000001, NOW(), 1067246875800000001, NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 1978000000000000222);
INSERT INTO sys_menu (id, pid, name, url, permissions, menu_type, icon, sort, creator, create_date, updater, update_date)
SELECT 1978000000000000223, 1978000000000000103, '修改', '', 'mall:pointsrule:update', 1, '', 2, 1067246875800000001, NOW(), 1067246875800000001, NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 1978000000000000223);
INSERT INTO sys_menu (id, pid, name, url, permissions, menu_type, icon, sort, creator, create_date, updater, update_date)
SELECT 1978000000000000224, 1978000000000000103, '删除', '', 'mall:pointsrule:delete', 1, '', 3, 1067246875800000001, NOW(), 1067246875800000001, NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 1978000000000000224);

INSERT INTO sys_menu (id, pid, name, url, permissions, menu_type, icon, sort, creator, create_date, updater, update_date)
SELECT 1978000000000000231, 1978000000000000104, '查看', '', 'mall:pointslog:page', 1, '', 0, 1067246875800000001, NOW(), 1067246875800000001, NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 1978000000000000231);

INSERT INTO sys_menu (id, pid, name, url, permissions, menu_type, icon, sort, creator, create_date, updater, update_date)
SELECT 1978000000000000241, 1978000000000000105, '查看', '', 'mall:order:page,mall:order:info', 1, '', 0, 1067246875800000001, NOW(), 1067246875800000001, NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 1978000000000000241);
INSERT INTO sys_menu (id, pid, name, url, permissions, menu_type, icon, sort, creator, create_date, updater, update_date)
SELECT 1978000000000000242, 1978000000000000105, '发货', '', 'mall:order:ship', 1, '', 1, 1067246875800000001, NOW(), 1067246875800000001, NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 1978000000000000242);
INSERT INTO sys_menu (id, pid, name, url, permissions, menu_type, icon, sort, creator, create_date, updater, update_date)
SELECT 1978000000000000243, 1978000000000000105, '标记支付', '', 'mall:order:paid', 1, '', 2, 1067246875800000001, NOW(), 1067246875800000001, NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 1978000000000000243);
