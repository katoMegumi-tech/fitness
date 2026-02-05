# 需求文档

## 简介

健身指导系统是一个基于Vue3 + Spring Boot + MySQL的综合性健身服务平台，旨在连接用户、教练和管理员三方，提供从教练认证、用户信息管理、个性化训练计划制定到日常打卡反馈的全流程健身指导服务。系统采用前后端分离架构，支持多角色协同工作，实现科学化、个性化的健身管理。

## 术语表

- **System**: 健身指导系统
- **User**: 普通用户，使用系统获取健身指导服务的个人
- **Coach**: 教练，提供专业健身指导服务的认证专业人员
- **Admin**: 管理员，负责审核教练资质、健身计划和科普内容的系统管理人员
- **Certification**: 教练职业资格证书及相关资质材料
- **Binding**: 用户与教练之间的服务关系
- **Fitness_Plan**: 教练为用户定制的包含饮食方案和运动计划的综合健身方案
- **Check_In**: 用户每日记录健身执行情况的打卡行为
- **Popular_Science**: 教练发布的健身科普类内容
- **Notification**: 系统内的消息通知
- **Body_Data**: 用户的身体数据，包括身高、体重、体脂率等
- **Audit**: 管理员对教练资质、健身计划、科普内容的审核流程

## 需求

### 需求 1: 教练入驻与认证管理

**用户故事:** 作为教练，我希望能够提交我的职业资格证书和教学履历，以便获得系统服务权限并为用户提供专业指导。

#### 验收标准

1. WHEN 教练提交资质材料（包括证书图片、教学履历、专业领域等信息）THEN System SHALL 创建认证审核记录并将状态设置为待审核
2. WHEN 管理员审核教练资质 THEN System SHALL 允许管理员标记为通过或拒绝，并记录审核意见
3. WHEN 教练资质审核通过 THEN System SHALL 更新教练账户状态为已认证并发送通知
4. WHEN 教练资质审核被拒绝 THEN System SHALL 保持教练账户为未认证状态并发送拒绝原因通知
5. WHEN 已认证教练更新资质信息 THEN System SHALL 创建新的审核记录并重新进入审核流程
6. THE System SHALL 存储教练的所有历史审核记录以供追溯

### 需求 2: 用户基础信息管理

**用户故事:** 作为用户，我希望能够录入和管理我的身体数据和健身偏好，以便教练为我制定个性化的健身计划。

#### 验收标准

1. WHEN 用户首次注册 THEN System SHALL 要求用户录入基础身体数据（身高、体重、体脂率、年龄、性别）
2. WHEN 用户录入健身目标信息 THEN System SHALL 保存目标类型（减脂、增肌、塑形等）、目标体重、期望达成时间
3. WHEN 用户录入运动偏好 THEN System SHALL 保存可用时间段、运动类型偏好、运动强度偏好
4. WHEN 用户录入健康限制信息 THEN System SHALL 保存过敏源、运动禁忌、既往病史等信息
5. WHEN 用户更新身体数据 THEN System SHALL 创建历史记录并保留数据变化轨迹
6. THE System SHALL 验证所有数值型数据的合理性范围（如体重20-300kg，体脂率5-60%）

### 需求 3: 用户-教练绑定管理

**用户故事:** 作为用户，我希望能够选择合适的教练并建立服务关系，以便获得专业的健身指导。

#### 验收标准

1. WHEN 用户浏览教练列表 THEN System SHALL 仅显示已认证的教练及其专业领域、服务人数等信息
2. WHEN 用户向教练发起绑定申请 THEN System SHALL 创建绑定申请记录并发送通知给教练
3. WHEN 教练接受绑定申请 THEN System SHALL 创建绑定关系记录并更新双方的绑定状态
4. WHEN 教练拒绝绑定申请 THEN System SHALL 更新申请状态为已拒绝并通知用户
5. WHEN 用户或教练发起解绑操作 THEN System SHALL 终止绑定关系并更新相关记录状态
6. THE System SHALL 限制每个用户同时只能绑定一个教练
7. WHEN 用户已有绑定关系 THEN System SHALL 阻止新的绑定申请直到解绑

### 需求 4: 健身计划制定与审核

**用户故事:** 作为教练，我希望能够基于用户的身体数据和目标制定个性化健身计划，经审核后推送给用户执行。

#### 验收标准

1. WHEN 教练为已绑定用户制定健身计划 THEN System SHALL 要求包含饮食方案、运动计划、计划周期、注意事项等完整信息
2. WHEN 教练提交健身计划 THEN System SHALL 创建计划记录并设置状态为待审核
3. WHEN 管理员审核健身计划 THEN System SHALL 允许标记为通过或拒绝，并记录审核意见
4. WHEN 健身计划审核通过 THEN System SHALL 更新计划状态为已发布并发送通知给用户
5. WHEN 健身计划审核被拒绝 THEN System SHALL 通知教练并说明拒绝原因
6. WHEN 教练调整已发布的计划 THEN System SHALL 创建新版本计划并重新进入审核流程
7. THE System SHALL 保留计划的所有历史版本以供对比

### 需求 5: 健身打卡与执行跟踪

**用户故事:** 作为用户，我希望能够每日记录我的健身执行情况，以便教练了解我的进展并提供反馈。

#### 验收标准

1. WHEN 用户进行每日打卡 THEN System SHALL 记录打卡日期、运动完成度、饮食执行情况、当日体重和体脂率
2. WHEN 用户上传打卡图片或视频 THEN System SHALL 存储媒体文件并关联到打卡记录
3. WHEN 用户提交打卡记录 THEN System SHALL 发送通知给绑定的教练
4. WHEN 教练查看用户打卡记录 THEN System SHALL 显示完整的打卡信息和历史趋势
5. WHEN 教练对打卡进行点评 THEN System SHALL 保存点评内容并发送通知给用户
6. WHEN 用户查询身体数据变化 THEN System SHALL 生成体重、体脂率等数据的时间序列图表
7. THE System SHALL 限制用户每天只能提交一次打卡记录

### 需求 6: 健身科普内容管理

**用户故事:** 作为教练，我希望能够发布健身科普内容，帮助用户获取专业的健身知识。

#### 验收标准

1. WHEN 教练创建科普文章 THEN System SHALL 要求包含标题、内容、分类（运动知识、饮食建议、康复指导、器械使用等）、封面图等信息
2. WHEN 教练提交科普文章 THEN System SHALL 创建文章记录并设置状态为待审核
3. WHEN 管理员审核科普文章 THEN System SHALL 允许标记为通过或拒绝，并记录审核意见
4. WHEN 科普文章审核通过 THEN System SHALL 更新文章状态为已发布并在科普列表中展示
5. WHEN 科普文章审核被拒绝 THEN System SHALL 通知教练并说明拒绝原因
6. WHEN 用户浏览科普文章 THEN System SHALL 显示已发布的文章并记录浏览次数
7. WHEN 用户对文章进行点赞或收藏 THEN System SHALL 记录互动行为并更新文章统计数据
8. WHEN 管理员对已发布文章进行管理 THEN System SHALL 支持下架、置顶、推荐等操作

### 需求 7: 消息通知系统

**用户故事:** 作为系统用户，我希望能够及时收到相关的通知消息，以便了解系统中的重要事件。

#### 验收标准

1. WHEN 发生绑定申请事件 THEN System SHALL 创建通知记录并发送给目标教练
2. WHEN 发生审核结果事件 THEN System SHALL 创建通知记录并发送给相关用户或教练
3. WHEN 发生计划推送事件 THEN System SHALL 创建通知记录并发送给目标用户
4. WHEN 发生教练点评事件 THEN System SHALL 创建通知记录并发送给目标用户
5. WHEN 用户查看通知 THEN System SHALL 更新通知状态为已读
6. THE System SHALL 保留用户最近30天的通知记录
7. WHEN 用户登录系统 THEN System SHALL 显示未读通知数量

### 需求 8: 用户认证与权限管理

**用户故事:** 作为系统管理员，我希望系统能够区分不同角色的用户并控制其访问权限，以确保系统安全和功能正确使用。

#### 验收标准

1. WHEN 用户注册账户 THEN System SHALL 要求提供用户名、密码、邮箱、手机号等基本信息
2. WHEN 用户登录系统 THEN System SHALL 验证用户名和密码的正确性
3. WHEN 用户登录成功 THEN System SHALL 生成会话令牌并返回用户角色信息
4. WHEN 用户访问功能模块 THEN System SHALL 验证用户角色是否有权限访问该模块
5. THE System SHALL 限制普通用户只能访问用户相关功能
6. THE System SHALL 限制教练只能访问教练相关功能和已绑定用户的数据
7. THE System SHALL 允许管理员访问所有审核和管理功能
8. WHEN 用户会话过期 THEN System SHALL 要求用户重新登录

### 需求 9: 数据查询与统计

**用户故事:** 作为教练，我希望能够查看我的学员数据统计，以便更好地了解服务效果和调整指导策略。

#### 验收标准

1. WHEN 教练查询学员列表 THEN System SHALL 显示所有已绑定用户的基本信息和当前状态
2. WHEN 教练查看学员详情 THEN System SHALL 显示该学员的完整身体数据、健身目标、执行计划和打卡历史
3. WHEN 教练查询打卡统计 THEN System SHALL 计算学员的打卡率、计划完成度等指标
4. WHEN 管理员查询系统统计 THEN System SHALL 显示用户总数、教练总数、活跃用户数、待审核项目数等关键指标
5. WHEN 用户查询个人数据 THEN System SHALL 显示身体数据变化趋势图和健身目标达成进度

### 需求 10: 数据安全与隐私保护

**用户故事:** 作为用户，我希望我的个人信息和健康数据得到妥善保护，不被未授权访问。

#### 验收标准

1. THE System SHALL 对用户密码进行加密存储
2. THE System SHALL 对敏感的健康数据进行访问控制
3. WHEN 教练访问用户数据 THEN System SHALL 验证是否存在有效的绑定关系
4. WHEN 用户解绑教练 THEN System SHALL 撤销教练对该用户数据的访问权限
5. THE System SHALL 记录所有敏感数据的访问日志
6. WHEN 用户删除账户 THEN System SHALL 匿名化或删除用户的个人数据
