package com.exemptenjoy.system.domain;
import com.exemptenjoy.common.core.domain.BaseEntity;
public class EeRule extends BaseEntity {
    private static final long serialVersionUID = 1L;
    private Long ruleId;
    private Long policyId;
    private String ruleName;
    private String conditionExpr;
    private String pdfUrl;
    private String pdfName;
    private String status;
    private String remark;
    private String policyName;
    public void setRuleId(Long ruleId) { this.ruleId = ruleId; }
    public Long getRuleId() { return ruleId; }
    public void setPolicyId(Long policyId) { this.policyId = policyId; }
    public Long getPolicyId() { return policyId; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }
    public String getRuleName() { return ruleName; }
    public void setConditionExpr(String conditionExpr) { this.conditionExpr = conditionExpr; }
    public String getConditionExpr() { return conditionExpr; }
    public void setPdfUrl(String pdfUrl) { this.pdfUrl = pdfUrl; }
    public String getPdfUrl() { return pdfUrl; }
    public void setPdfName(String pdfName) { this.pdfName = pdfName; }
    public String getPdfName() { return pdfName; }
    public void setStatus(String status) { this.status = status; }
    public String getStatus() { return status; }
    public void setRemark(String remark) { this.remark = remark; }
    public String getRemark() { return remark; }
    public void setPolicyName(String policyName) { this.policyName = policyName; }
    public String getPolicyName() { return policyName; }
}
