-- 所有环境都必须存在的核心数据
-- 序列生成器的业务类型配置
INSERT IGNORE INTO sequence_generator (
  biz_type,
  current_value,
  reset_strategy,
  created_at,
  updated_at
)
VALUES
  (
    'inspection_device',
    0,
    'DAILY',
    NOW (),
    NOW ()
  ),
  (
    'project_internal',
    0,
    'NONE',
    NOW (),
    NOW ()
  );