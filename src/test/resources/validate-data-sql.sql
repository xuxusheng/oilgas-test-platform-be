-- 验证 data.sql 与 SequenceGenerator 实体对齐的 SQL 脚本
-- 这个脚本可以用来手动验证修改是否正确

-- 1. 检查 sequence_generator 表结构是否匹配实体
DESCRIBE sequence_generator;

-- 2. 检查预设数据是否正确插入
SELECT * FROM sequence_generator;

-- 3. 验证业务类型是否与 SequenceBizType 枚举匹配
-- 应该能看到 'inspection_device' 和 'project_internal'
SELECT biz_type, current_value, reset_strategy, created_at, updated_at
FROM sequence_generator
ORDER BY id;

-- 4. 检查 ResetStrategy 枚举值是否有效
-- 应该能看到 DAILY, NONE, YEARLY 这些值
SELECT DISTINCT reset_strategy FROM sequence_generator;