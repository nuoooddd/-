package com.exemptenjoy.system.domain.vo;

import java.util.List;

/**
 * 匹配结果 - 政策匹配详情（含规则拆解）
 */
public class MatchDetailVo {
    private Long policyId;
    private String policyName;
    private String policyType;
    private java.math.BigDecimal amount;
    private String publishDept;
    private String remark;
    private boolean matched;
    private Long recordId;
    private String recordStatus;
    private String auditStatus;
    private java.math.BigDecimal fundAmount;
    private List<RuleDetailVo> rules;

    public Long getPolicyId() { return policyId; }
    public void setPolicyId(Long policyId) { this.policyId = policyId; }
    public String getPolicyName() { return policyName; }
    public void setPolicyName(String policyName) { this.policyName = policyName; }
    public String getPolicyType() { return policyType; }
    public void setPolicyType(String policyType) { this.policyType = policyType; }
    public java.math.BigDecimal getAmount() { return amount; }
    public void setAmount(java.math.BigDecimal amount) { this.amount = amount; }
    public String getPublishDept() { return publishDept; }
    public void setPublishDept(String publishDept) { this.publishDept = publishDept; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public boolean isMatched() { return matched; }
    public void setMatched(boolean matched) { this.matched = matched; }
    public Long getRecordId() { return recordId; }
    public void setRecordId(Long recordId) { this.recordId = recordId; }
    public String getRecordStatus() { return recordStatus; }
    public void setRecordStatus(String recordStatus) { this.recordStatus = recordStatus; }
    public String getAuditStatus() { return auditStatus; }
    public void setAuditStatus(String auditStatus) { this.auditStatus = auditStatus; }
    public java.math.BigDecimal getFundAmount() { return fundAmount; }
    public void setFundAmount(java.math.BigDecimal fundAmount) { this.fundAmount = fundAmount; }
    public List<RuleDetailVo> getRules() { return rules; }
    public void setRules(List<RuleDetailVo> rules) { this.rules = rules; }
}
