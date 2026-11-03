-- ============================================================================
-- 删除商城 / IoT 模块遗留的定时任务
--
-- 这些 Job 的 Handler 所在模块已被删除，保留会导致后续启用时找不到 Bean。
-- ============================================================================

DELETE FROM infra_job WHERE handler_name IN (
    'tradeOrderAutoCancelJob',
    'tradeOrderAutoReceiveJob',
    'tradeOrderAutoCommentJob',
    'iotDeviceOfflineCheckJob',
    'iotOtaUpgradeJob',
    'productStatisticsJob'
);
