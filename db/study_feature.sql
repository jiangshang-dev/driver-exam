-- 驾考学习功能表
-- 顺序刷题 /drive/driveTopicList、模拟考试 /drive/examDriveQuestion 的查询语句不改。
-- 下面只给题库补组合索引，让现有条件（subject、exam_type、tags）走索引。
-- 每日一练走独立接口，按主键范围取固定条数，不加载整库。

SET NAMES utf8mb4;

-- 题库查询索引：已存在则跳过
SET @idx_exists := (
    SELECT COUNT(1)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'ap_drive_question'
      AND index_name = 'idx_subject_exam_tags'
);
SET @idx_sql := IF(
    @idx_exists = 0,
    'ALTER TABLE ap_drive_question ADD INDEX idx_subject_exam_tags (subject, exam_type, tags, id)',
    'SELECT 1'
);
PREPARE idx_stmt FROM @idx_sql;
EXECUTE idx_stmt;
DEALLOCATE PREPARE idx_stmt;

-- 每日一练按 subject + exam_type + id 取一小段，避免整表排序
SET @range_idx_exists := (
    SELECT COUNT(1)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'ap_drive_question'
      AND index_name = 'idx_subject_exam_id'
);
SET @range_idx_sql := IF(
    @range_idx_exists = 0,
    'ALTER TABLE ap_drive_question ADD INDEX idx_subject_exam_id (subject, exam_type, id)',
    'SELECT 1'
);
PREPARE range_idx_stmt FROM @range_idx_sql;
EXECUTE range_idx_stmt;
DEALLOCATE PREPARE range_idx_stmt;

CREATE TABLE IF NOT EXISTS ap_study_checkin (
    id             BIGINT      NOT NULL COMMENT '主键',
    user_id        BIGINT      NOT NULL COMMENT '用户ID',
    checkin_date   DATE        NOT NULL COMMENT '打卡日期',
    subject        TINYINT     NOT NULL DEFAULT 1 COMMENT '科目：1科目一，4科目四',
    exam_type      VARCHAR(8)  NOT NULL DEFAULT 'c1' COMMENT '车型',
    question_count INT         NOT NULL DEFAULT 0 COMMENT '完成题数',
    correct_count  INT         NOT NULL DEFAULT 0 COMMENT '答对题数',
    gmt_create     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    gmt_modified   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_date_subject (user_id, checkin_date, subject, exam_type),
    KEY idx_user_date (user_id, checkin_date)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='学习打卡';

CREATE TABLE IF NOT EXISTS ap_exam_plan (
    id           BIGINT      NOT NULL COMMENT '主键',
    user_id      BIGINT      NOT NULL COMMENT '用户ID',
    exam_type    VARCHAR(8)  NOT NULL DEFAULT 'c1' COMMENT '车型',
    subject      TINYINT     NOT NULL DEFAULT 1 COMMENT '科目：1科目一，4科目四',
    exam_date    DATE        NOT NULL COMMENT '计划考试日期',
    target_score INT         NOT NULL DEFAULT 90 COMMENT '目标分数',
    daily_goal   INT         NOT NULL DEFAULT 20 COMMENT '每日题量',
    remark       VARCHAR(255)         DEFAULT NULL COMMENT '备注',
    gmt_create   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    gmt_modified DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    KEY idx_exam_plan_user (user_id, gmt_modified)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='考试计划';

CREATE TABLE IF NOT EXISTS ap_knowledge_card (
    id           BIGINT       NOT NULL COMMENT '主键',
    subject      TINYINT      NOT NULL DEFAULT 1 COMMENT '科目：1科目一，4科目四',
    category     VARCHAR(32)  NOT NULL COMMENT '分类',
    title        VARCHAR(64)  NOT NULL COMMENT '标题',
    content      VARCHAR(1000) NOT NULL COMMENT '速记内容',
    sort_order   INT          NOT NULL DEFAULT 0 COMMENT '排序',
    is_deleted   TINYINT      NOT NULL DEFAULT 0 COMMENT '是否删除：0否 1是',
    gmt_create   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    gmt_modified DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    KEY idx_knowledge_subject (subject, is_deleted, sort_order)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='考点速记';

CREATE TABLE IF NOT EXISTS ap_practice_progress (
    id              BIGINT      NOT NULL COMMENT '主键',
    user_id         BIGINT      NOT NULL COMMENT '用户ID',
    subject         TINYINT     NOT NULL COMMENT '科目',
    exam_type       VARCHAR(8)  NOT NULL COMMENT '车型',
    question_type   VARCHAR(64) NOT NULL DEFAULT '' COMMENT '题型，空表示全部',
    last_index      INT         NOT NULL DEFAULT 0 COMMENT '上次做到的下标',
    answered_count  INT         NOT NULL DEFAULT 0 COMMENT '已答题数',
    correct_count   INT         NOT NULL DEFAULT 0 COMMENT '答对题数',
    gmt_create      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    gmt_modified    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_progress (user_id, subject, exam_type, question_type)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='顺序练习进度，刷题列表接口不读这张表';

INSERT INTO ap_knowledge_card (id, subject, category, title, content, sort_order)
SELECT 8800000000000000001, 1, '标志颜色', '三种标志一眼分开', '警告标志是黄底、黑边、黑图案；禁令标志是白底、红圈、黑图案；指示标志是蓝底、白图案。看到黄色先减速，看到红圈先停或禁，看到蓝色按箭头走。', 10
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ap_knowledge_card WHERE id = 8800000000000000001);

INSERT INTO ap_knowledge_card (id, subject, category, title, content, sort_order)
SELECT 8800000000000000002, 1, '速度', '没有限速标志时的默认速度', '城市道路：没有中心线最高 30，有中心线最高 50。公路：没有中心线最高 40，有中心线最高 70。高速公路小客车最高 120，最低 60。', 20
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ap_knowledge_card WHERE id = 8800000000000000002);

INSERT INTO ap_knowledge_card (id, subject, category, title, content, sort_order)
SELECT 8800000000000000003, 1, '灯光', '会车和跟车用近光', '夜间会车、跟车、通过急弯、坡路、拱桥、人行横道或没有交通信号的路口，改用近光灯。远光灯只在没有路灯且对向无车时使用。雾天打开雾灯和危险报警闪光灯。', 30
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ap_knowledge_card WHERE id = 8800000000000000003);

INSERT INTO ap_knowledge_card (id, subject, category, title, content, sort_order)
SELECT 8800000000000000004, 1, '数字', '实习期和记分周期', '初次领证实习期 12 个月，车上粘贴实习标志。记分周期是 12 个月，满分 12 分。饮酒后驾驶记 12 分并暂扣驾驶证，醉酒驾驶由公安机关追究刑责。', 40
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ap_knowledge_card WHERE id = 8800000000000000004);

INSERT INTO ap_knowledge_card (id, subject, category, title, content, sort_order)
SELECT 8800000000000000005, 1, '让行', '没有信号灯的路口', '没有交通信号的交叉路口要减速慢行，让右方道路来车先行。转弯的机动车让直行的车辆、行人先行。右转弯让左转弯的车先行。', 50
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ap_knowledge_card WHERE id = 8800000000000000005);

INSERT INTO ap_knowledge_card (id, subject, category, title, content, sort_order)
SELECT 8800000000000000006, 1, '距离', '停车和跟车距离', '普通公路故障警告标志设在车后 50 米到 100 米。高速公路设在 150 米以外。冰雪、泥泞、涉水、坡道不要紧跟前车，加大纵向安全距离。', 60
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ap_knowledge_card WHERE id = 8800000000000000006);

INSERT INTO ap_knowledge_card (id, subject, category, title, content, sort_order)
SELECT 8800000000000000007, 4, '应急', '爆胎时不要猛踩刹车', '先握稳方向盘，松抬加速踏板，轻踏制动，让车慢慢靠边。高速爆胎更不能急刹、急打方向。车速降下来再打开危险报警闪光灯，人员撤到护栏外侧。', 10
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ap_knowledge_card WHERE id = 8800000000000000007);

INSERT INTO ap_knowledge_card (id, subject, category, title, content, sort_order)
SELECT 8800000000000000008, 4, '应急', '车辆着火和落水', '着火先停车断电，用灭火器对准火焰根部。发动机着火不能开引擎盖猛扑，先从缝隙喷射。落水先解安全带、开车窗或破窗，水位没过车窗再开门。', 20
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ap_knowledge_card WHERE id = 8800000000000000008);

INSERT INTO ap_knowledge_card (id, subject, category, title, content, sort_order)
SELECT 8800000000000000009, 4, '急救', '伤员先救命再转运', '先判断意识、呼吸和出血。大出血先止血，骨折先固定再搬运。没有呼吸心跳先心肺复苏。怀疑脊柱损伤时保持头颈躯干一条直线。', 30
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ap_knowledge_card WHERE id = 8800000000000000009);

INSERT INTO ap_knowledge_card (id, subject, category, title, content, sort_order)
SELECT 8800000000000000010, 4, '高速', '高速故障停车顺序', '打开危险报警闪光灯，车上人员戴好反光衣，从右侧车门下车，撤到护栏外。警告标志放在来车方向 150 米以外，再报警。不要在行车道检查或修理。', 40
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ap_knowledge_card WHERE id = 8800000000000000010);

INSERT INTO ap_knowledge_card (id, subject, category, title, content, sort_order)
SELECT 8800000000000000011, 4, '恶劣天气', '雾雨雪三种开法', '雾天开雾灯、危险报警闪光灯，靠右慢行。雨天积水减速，不要紧急制动和急转。冰雪路面低速匀速，避免空挡滑行，转弯提前轻点刹车。', 50
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ap_knowledge_card WHERE id = 8800000000000000011);

INSERT INTO ap_knowledge_card (id, subject, category, title, content, sort_order)
SELECT 8800000000000000012, 4, '文明驾驶', '礼让行人是判断题高频', '通过人行横道必须减速，遇行人正在通过要停车让行。学校、公交站、窄路会车减速靠右。不抢黄灯，不加塞，不向车外抛扔物品。', 60
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ap_knowledge_card WHERE id = 8800000000000000012);

-- 学习记录 / 考试记录 后台页面菜单（学习管理下）
UPDATE sys_menu SET url = '/learning/studyRecord' WHERE pid = 1973335724252581890 AND name = '学习记录';
UPDATE sys_menu SET url = '/learning/examRecord' WHERE pid = 1973335724252581890 AND name = '考试记录';

INSERT INTO sys_menu (id, pid, name, url, permissions, menu_type, icon, sort, creator, create_date, updater, update_date)
SELECT 1978000000000000301, m.id, '查看', '', 'study:record:page', 1, '', 0, 1067246875800000001, NOW(), 1067246875800000001, NOW()
FROM sys_menu m
WHERE m.pid = 1973335724252581890 AND m.name = '学习记录'
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 1978000000000000301);

INSERT INTO sys_menu (id, pid, name, url, permissions, menu_type, icon, sort, creator, create_date, updater, update_date)
SELECT 1978000000000000302, m.id, '查看', '', 'exam:record:page', 1, '', 0, 1067246875800000001, NOW(), 1067246875800000001, NOW()
FROM sys_menu m
WHERE m.pid = 1973335724252581890 AND m.name = '考试记录'
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 1978000000000000302);
