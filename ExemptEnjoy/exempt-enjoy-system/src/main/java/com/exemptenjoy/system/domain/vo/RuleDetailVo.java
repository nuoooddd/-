package com.exemptenjoy.system.domain.vo;

import java.util.List;

/**
 * 匹配结果 - 规则详情
 */
public class RuleDetailVo {
    private Long ruleId;
    private String ruleName;
    private String conditionExpr;
    private boolean passed;   // 该规则整体是否通过
    private List<ConditionDetailVo> conditions;

    public Long getRuleId() { return ruleId; }
    public void setRuleId(Long ruleId) { this.ruleId = ruleId; }
    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }
    public String getConditionExpr() { return conditionExpr; }
    public void setConditionExpr(String conditionExpr) { this.conditionExpr = conditionExpr; }
    public boolean isPassed() { return passed; }
    public void setPassed(boolean passed) { this.passed = passed; }
    public List<ConditionDetailVo> getConditions() { return conditions; }
    public void setConditions(List<ConditionDetailVo> conditions) { this.conditions = conditions; }
}
