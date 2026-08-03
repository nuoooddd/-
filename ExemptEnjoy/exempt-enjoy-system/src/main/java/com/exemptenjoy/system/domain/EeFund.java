package com.exemptenjoy.system.domain;
import com.exemptenjoy.common.core.domain.BaseEntity;
import java.math.BigDecimal;
public class EeFund extends BaseEntity {
    private static final long serialVersionUID = 1L;
    private Long fundId;
    private Long policyId;
    private BigDecimal totalBudget;
    private BigDecimal usedAmount;
    private String policyName;
    public void setFundId(Long fundId) { this.fundId = fundId; }
    public Long getFundId() { return fundId; }
    public void setPolicyId(Long policyId) { this.policyId = policyId; }
    public Long getPolicyId() { return policyId; }
    public void setTotalBudget(BigDecimal totalBudget) { this.totalBudget = totalBudget; }
    public BigDecimal getTotalBudget() { return totalBudget; }
    public void setUsedAmount(BigDecimal usedAmount) { this.usedAmount = usedAmount; }
    public BigDecimal getUsedAmount() { return usedAmount; }
    public void setPolicyName(String policyName) { this.policyName = policyName; }
    public String getPolicyName() { return policyName; }
}
