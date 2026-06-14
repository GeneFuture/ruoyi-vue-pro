package cn.iocoder.yudao.module.maritime.service.risk;

/**
 * 风控检查结果
 */
public record RiskCheckResult(boolean passed, String ruleCode, String reason) {

    public static RiskCheckResult pass() {
        return new RiskCheckResult(true, null, null);
    }

    public static RiskCheckResult fail(String ruleCode, String reason) {
        return new RiskCheckResult(false, ruleCode, reason);
    }
}
