package com.exemptenjoy.system.domain.vo;

/**
 * 匹配结果 - 单个条件详情
 */
public class ConditionDetailVo {
    private String field;
    private String operator;
    private String expectedValue;
    private String actualValue;
    private boolean passed;
    private String expression;  // 原始表达式如 "industry == 'High-tech'"

    public String getField() { return field; }
    public void setField(String field) { this.field = field; }
    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }
    public String getExpectedValue() { return expectedValue; }
    public void setExpectedValue(String expectedValue) { this.expectedValue = expectedValue; }
    public String getActualValue() { return actualValue; }
    public void setActualValue(String actualValue) { this.actualValue = actualValue; }
    public boolean isPassed() { return passed; }
    public void setPassed(boolean passed) { this.passed = passed; }
    public String getExpression() { return expression; }
    public void setExpression(String expression) { this.expression = expression; }
}
