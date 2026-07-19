-- ============================================
-- 健身指导系统数据库建表脚本
-- 版本：2.0（精简版 - 12张表）
-- 说明：统一账号主体 + 完整业务流程 + 审核标准化
-- ============================================

-- 设置字符集
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================
-- 1. 核心账号与角色管理
-- ============================================

-- 1.1 用户表（统一账号主体）
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `user_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
  `account` VARCHAR(20) NOT NULL UNIQUE COMMENT '账号',
  `password` VARCHAR(100) NOT NULL COMMENT '密码（BCrypt加密）',
  `name` VARCHAR(50) NOT NULL COMMENT '姓名',
  `age` TINYINT UNSIGNED DEFAULT NULL COMMENT '年龄',
  `gender` CHAR(1) DEFAULT NULL COMMENT '性别：M/F',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `email` VARCHAR(50) DEFAULT NULL COMMENT '邮箱',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像路径',
  `role` VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色：USER/COACH/ADMIN',
  `account_status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '账号状态：ACTIVE/FROZEN/DELETED',
  `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX `idx_account` (`account`),
  INDEX `idx_role` (`role`),
  INDEX `idx_status` (`account_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='统一账号主体表';

-- 1.2 教练扩展表
DROP TABLE IF EXISTS `coach_profile`;
CREATE TABLE `coach_profile` (
  `user_id` BIGINT PRIMARY KEY COMMENT '教练用户ID（FK -> user.user_id）',
  `certs_path` TEXT DEFAULT NULL COMMENT '证书附件路径（JSON数组）',
  `introduction` TEXT DEFAULT NULL COMMENT '个人简介',
  `specialties` VARCHAR(200) DEFAULT NULL COMMENT '擅长领域（如：减脂/增肌/康复训练）',
  `years_of_experience` TINYINT UNSIGNED DEFAULT 0 COMMENT '从业年限',
  `certification_status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '认证状态：PENDING/APPROVED/REJECTED',
  `rating` DECIMAL(3,2) DEFAULT 5.00 COMMENT '教练评分（1-5分）',
  `total_students` INT UNSIGNED DEFAULT 0 COMMENT '累计服务学员数',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX `idx_cert_status` (`certification_status`),
  INDEX `idx_rating` (`rating`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教练扩展信息表';

-- 1.3 教练资质审核记录表
DROP TABLE IF EXISTS `t_cert_audit_record`;
CREATE TABLE `t_cert_audit_record` (
  `record_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '审核记录ID',
  `coach_user_id` BIGINT NOT NULL COMMENT '申请教练ID（FK -> user.user_id）',
  `audit_status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '审核状态：PENDING/APPROVED/REJECTED',
  `audit_opinion` TEXT DEFAULT NULL COMMENT '审核意见',
  `admin_id` BIGINT DEFAULT NULL COMMENT '审核管理员ID（FK -> user.user_id）',
  `apply_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  `audit_time` DATETIME DEFAULT NULL COMMENT '审核时间',
  INDEX `idx_coach_id` (`coach_user_id`),
  INDEX `idx_status` (`audit_status`),
  INDEX `idx_apply_time` (`apply_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教练资质审核记录表';

-- ============================================
-- 2. 用户-教练绑定管理
-- ============================================

-- 2.1 绑定申请表
DROP TABLE IF EXISTS `t_bind_apply`;
CREATE TABLE `t_bind_apply` (
  `apply_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '申请ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID（FK -> user.user_id）',
  `coach_user_id` BIGINT NOT NULL COMMENT '教练ID（FK -> user.user_id）',
  `apply_reason` VARCHAR(500) DEFAULT NULL COMMENT '申请理由',
  `apply_status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '申请状态：PENDING/APPROVED/REJECTED/CANCELLED',
  `reject_reason` VARCHAR(200) DEFAULT NULL COMMENT '拒绝理由',
  `apply_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  `handle_time` DATETIME DEFAULT NULL COMMENT '处理时间',
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_coach_id` (`coach_user_id`),
  INDEX `idx_status` (`apply_status`),
  INDEX `idx_apply_time` (`apply_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户-教练绑定申请表';

-- 2.2 绑定关系表
DROP TABLE IF EXISTS `t_bind_record`;
CREATE TABLE `t_bind_record` (
  `bind_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '绑定ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID（FK -> user.user_id）',
  `coach_user_id` BIGINT NOT NULL COMMENT '教练ID（FK -> user.user_id）',
  `bind_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '绑定时间',
  `unbind_time` DATETIME DEFAULT NULL COMMENT '解绑时间（NULL表示仍绑定）',
  `unbind_reason` VARCHAR(200) DEFAULT NULL COMMENT '解绑原因',
  UNIQUE KEY `uk_current_bind` (`user_id`, `unbind_time`),
  INDEX `idx_coach_id` (`coach_user_id`),
  INDEX `idx_bind_time` (`bind_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户-教练绑定关系表';

-- ============================================
-- 3. 用户身体数据管理
-- ============================================

-- 3.1 用户身体基础数据表（目标数据）
DROP TABLE IF EXISTS `t_user_body_base_data`;
CREATE TABLE `t_user_body_base_data` (
  `data_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '数据ID',
  `user_id` BIGINT NOT NULL UNIQUE COMMENT 'FK -> user.user_id',
  `height` DECIMAL(5,2) DEFAULT NULL COMMENT '身高（米）',
  `target_weight` DECIMAL(5,2) DEFAULT NULL COMMENT '目标体重（kg）',
  `target_body_fat_rate` DECIMAL(4,1) DEFAULT NULL COMMENT '目标体脂率（%）',
  `food_preference` TEXT DEFAULT NULL COMMENT '饮食偏好',
  `allergens` VARCHAR(100) DEFAULT NULL COMMENT '过敏源',
  `fitness_goal` VARCHAR(100) DEFAULT NULL COMMENT '健身目标：减脂/增肌/塑形/保持',
  `exercise_frequency` TINYINT UNSIGNED DEFAULT NULL COMMENT '每周运动频率（次）',
  `exercise_duration` TINYINT UNSIGNED DEFAULT NULL COMMENT '每次运动时长（分钟）',
  `health_conditions` TEXT DEFAULT NULL COMMENT '健康状况说明（疾病史、运动禁忌等）',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户身体基础数据表';

-- 3.2 用户身体数据历史记录表
DROP TABLE IF EXISTS `t_user_body_history`;
CREATE TABLE `t_user_body_history` (
  `history_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '历史记录ID',
  `user_id` BIGINT NOT NULL COMMENT 'FK -> user.user_id',
  `weight` DECIMAL(5,2) DEFAULT NULL COMMENT '体重（kg）',
  `body_fat_rate` DECIMAL(4,1) DEFAULT NULL COMMENT '体脂率（%）',
  `muscle_mass` DECIMAL(5,2) DEFAULT NULL COMMENT '肌肉量（kg）',
  `bmi` DECIMAL(4,2) DEFAULT NULL COMMENT 'BMI指数',
  `waistline` DECIMAL(5,2) DEFAULT NULL COMMENT '腰围（cm）',
  `record_date` DATE NOT NULL COMMENT '记录日期',
  `record_source` VARCHAR(20) DEFAULT 'MANUAL' COMMENT '记录来源：MANUAL/CHECK_IN/DEVICE',
  `remark` VARCHAR(200) DEFAULT NULL COMMENT '备注',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  INDEX `idx_user_date` (`user_id`, `record_date`),
  INDEX `idx_record_date` (`record_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户身体数据历史记录表';

-- ============================================
-- 4. 健身计划管理
-- ============================================

-- 4.1 健身计划表
DROP TABLE IF EXISTS `t_fitness_plan`;
CREATE TABLE `t_fitness_plan` (
  `plan_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '计划ID',
  `plan_no` VARCHAR(30) NOT NULL UNIQUE COMMENT '计划编号',
  `user_id` BIGINT NOT NULL COMMENT '计划所属用户（FK -> user.user_id）',
  `coach_user_id` BIGINT NOT NULL COMMENT '制定教练（FK -> user.user_id）',
  `plan_name` VARCHAR(100) NOT NULL COMMENT '计划名称',
  `fitness_goal` VARCHAR(100) NOT NULL COMMENT '健身目标',
  `exercise_plan` TEXT NOT NULL COMMENT '运动计划（JSON格式）',
  `diet_plan` TEXT NOT NULL COMMENT '饮食计划（JSON格式）',
  `plan_difficulty` VARCHAR(20) NOT NULL DEFAULT 'BEGINNER' COMMENT '计划难度：BEGINNER/INTERMEDIATE/ADVANCED',
  `plan_cycle` INT NOT NULL DEFAULT 7 COMMENT '计划周期（天）',
  `plan_start_time` DATETIME NOT NULL COMMENT '计划开始时间',
  `plan_end_time` DATETIME NULL COMMENT '计划结束时间',
  `plan_status` VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT '计划状态：DRAFT/PENDING/ACTIVE/COMPLETED/CANCELLED',
  `version` INT NOT NULL DEFAULT 1 COMMENT '计划版本号',
  `parent_plan_id` BIGINT DEFAULT NULL COMMENT '父计划ID（调整后的计划关联原计划）',
  `adjustment_reason` VARCHAR(500) DEFAULT NULL COMMENT '调整原因（记录最近一次调整）',
  -- 审核字段
  `audit_status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '审核状态：PENDING/APPROVED/REJECTED',
  `audit_admin_id` BIGINT DEFAULT NULL COMMENT '审核管理员ID',
  `audit_time` DATETIME DEFAULT NULL COMMENT '审核时间',
  `audit_remark` VARCHAR(200) DEFAULT NULL COMMENT '审核备注',
  -- 用户确认字段
  `user_confirm_status` VARCHAR(20) DEFAULT NULL COMMENT '用户确认状态：PENDING/APPROVED/REJECTED',
  `user_confirm_time` DATETIME DEFAULT NULL COMMENT '用户确认时间',
  `user_reject_reason` VARCHAR(200) DEFAULT NULL COMMENT '用户拒绝理由',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX `idx_plan_no` (`plan_no`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_coach_id` (`coach_user_id`),
  INDEX `idx_status` (`plan_status`),
  INDEX `idx_audit_status` (`audit_status`),
  INDEX `idx_start_time` (`plan_start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='健身计划表';

-- ============================================
-- 5. 打卡与执行管理
-- ============================================

-- 5.1 打卡记录表
DROP TABLE IF EXISTS `t_check_in_record`;
CREATE TABLE `t_check_in_record` (
  `check_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '打卡ID',
  `check_no` VARCHAR(30) NOT NULL UNIQUE COMMENT '打卡编号',
  `user_id` BIGINT NOT NULL COMMENT 'FK -> user.user_id',
  `plan_id` BIGINT NOT NULL COMMENT 'FK -> t_fitness_plan.plan_id',
  `check_type` VARCHAR(20) NOT NULL DEFAULT 'NORMAL' COMMENT '打卡类型：NORMAL/TARGET',
  `check_status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '打卡状态：PENDING=待审核, COMPLETED=已完结',
  `is_qualified` TINYINT(1) DEFAULT NULL COMMENT '是否达标：1=达标, 0=未达标',
  `exercise_completion` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '运动完成度（0-100）',
  `diet_completion` VARCHAR(20) NOT NULL DEFAULT 'INCOMPLETE' COMMENT '饮食完成度：COMPLETE/PARTIAL/INCOMPLETE',
  `check_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '打卡时间',
  -- 身体数据
  `weight` DECIMAL(5,2) DEFAULT NULL COMMENT '体重（kg）',
  `body_fat_rate` DECIMAL(4,1) DEFAULT NULL COMMENT '体脂率（%）',
  `muscle_mass` DECIMAL(5,2) DEFAULT NULL COMMENT '肌肉量（kg）',
  -- 附件与反馈
  `images` TEXT DEFAULT NULL COMMENT '打卡图片路径（JSON数组）',
  `videos` TEXT DEFAULT NULL COMMENT '打卡视频路径（JSON数组）',
  `user_remark` VARCHAR(500) DEFAULT NULL COMMENT '用户备注',
  `coach_comment` VARCHAR(500) DEFAULT NULL COMMENT '教练点评',
  `coach_comment_time` DATETIME DEFAULT NULL COMMENT '教练点评时间',
  CHECK (`exercise_completion` BETWEEN 0 AND 100),
  INDEX `idx_check_no` (`check_no`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_plan_id` (`plan_id`),
  INDEX `idx_check_time` (`check_time`),
  INDEX `idx_check_status` (`check_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='打卡记录表';

-- ============================================
-- 6. 健身科普内容管理
-- ============================================

-- 6.1 健身科普表
DROP TABLE IF EXISTS `t_fitness_popular_science`;
CREATE TABLE `t_fitness_popular_science` (
  `article_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '文章ID',
  `article_no` VARCHAR(30) NOT NULL UNIQUE COMMENT '文章编号',
  `title` VARCHAR(100) NOT NULL COMMENT '文章标题',
  `category` VARCHAR(50) NOT NULL COMMENT '文章分类：运动知识/饮食建议/康复指导/器械使用/其他',
  `content` TEXT NOT NULL COMMENT '文章内容',
  `cover_image` VARCHAR(255) DEFAULT NULL COMMENT '封面图片路径',
  `author_id` BIGINT NOT NULL COMMENT '作者ID（教练，FK -> user.user_id）',
  `tags` VARCHAR(200) DEFAULT NULL COMMENT '标签（逗号分隔）',
  -- 发布状态
  `publish_status` VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT '发布状态：DRAFT/PUBLISHED/OFFLINE',
  `is_top` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否置顶：0=否, 1=是',
  `top_time` DATETIME DEFAULT NULL COMMENT '置顶时间',
  `publish_time` DATETIME DEFAULT NULL COMMENT '发布时间',
  -- 审核字段
  `audit_status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '审核状态：PENDING/APPROVED/REJECTED',
  `audit_admin_id` BIGINT DEFAULT NULL COMMENT '审核管理员ID',
  `audit_time` DATETIME DEFAULT NULL COMMENT '审核时间',
  `audit_remark` VARCHAR(200) DEFAULT NULL COMMENT '审核备注',
  -- 统计字段
  `view_count` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '浏览量',
  `like_count` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '点赞数',
  `collect_count` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '收藏数',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX `idx_article_no` (`article_no`),
  INDEX `idx_author_id` (`author_id`),
  INDEX `idx_category` (`category`),
  INDEX `idx_publish_status` (`publish_status`),
  INDEX `idx_is_top` (`is_top`),
  INDEX `idx_publish_time` (`publish_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='健身科普表';

-- 6.2 科普文章互动表（点赞、收藏、浏览记录）
DROP TABLE IF EXISTS `t_article_interaction`;
CREATE TABLE `t_article_interaction` (
  `interaction_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '互动ID',
  `article_id` BIGINT NOT NULL COMMENT '文章ID（FK -> t_fitness_popular_science.article_id）',
  `user_id` BIGINT NOT NULL COMMENT '用户ID（FK -> user.user_id）',
  `interaction_type` VARCHAR(20) NOT NULL COMMENT '互动类型：LIKE/COLLECT/VIEW',
  `interaction_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '互动时间',
  UNIQUE KEY `uk_article_user_type` (`article_id`, `user_id`, `interaction_type`),
  INDEX `idx_article_id` (`article_id`),
  INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='科普文章互动表';

-- ============================================
-- 7. 消息通知管理
-- ============================================

DROP TABLE IF EXISTS `t_notification`;
CREATE TABLE `t_notification` (
  `notification_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '通知ID',
  `receiver_id` BIGINT NOT NULL COMMENT '接收者ID（FK -> user.user_id）',
  `sender_id` BIGINT DEFAULT NULL COMMENT '发送者ID（FK -> user.user_id，系统通知为NULL）',
  `notification_type` VARCHAR(30) NOT NULL COMMENT '通知类型：BIND_APPLY/BIND_RESULT/PLAN_AUDIT/PLAN_PUSH/CHECK_COMMENT/CERT_AUDIT/SYSTEM',
  `title` VARCHAR(100) NOT NULL COMMENT '通知标题',
  `content` VARCHAR(500) NOT NULL COMMENT '通知内容',
  `related_id` BIGINT DEFAULT NULL COMMENT '关联业务ID（如计划ID、申请ID等）',
  `is_read` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已读：0=未读, 1=已读',
  `read_time` DATETIME DEFAULT NULL COMMENT '阅读时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  INDEX `idx_receiver_id` (`receiver_id`),
  INDEX `idx_is_read` (`is_read`),
  INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息通知表';

-- ============================================
-- 数据库创建完成 - 共12张表
-- ============================================
