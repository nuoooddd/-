package com.exemptenjoy.system.domain;
import com.exemptenjoy.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.util.Date;
public class EePolicy extends BaseEntity {
    private Long policyId;
    private String policyName;
    private String policyType;
    private BigDecimal amount;
    private String publishDept;
    private String status;
    private String remark;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date startDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date endDate;
    private String pdfUrl;
    private String pdfName;
    public void setPolicyId(Long policyId) { this.policyId = policyId; }
    public Long getPolicyId() { return policyId; }
    public void setPolicyName(String policyName) { this.policyName = policyName; }
    public String getPolicyName() { return policyName; }
    public void setPolicyType(String policyType) { this.policyType = policyType; }
    public String getPolicyType() { return policyType; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public BigDecimal getAmount() { return amount; }
    public void setPublishDept(String publishDept) { this.publishDept = publishDept; }
    public String getPublishDept() { return publishDept; }
    public void setStatus(String status) { this.status = status; }
    public String getStatus() { return status; }
    public void setRemark(String remark) { this.remark = remark; }
    public String getRemark() { return remark; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }
    public Date getStartDate() { return startDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }
    public Date getEndDate() { return endDate; }
    public void setPdfUrl(String pdfUrl) { this.pdfUrl = pdfUrl; }
    public String getPdfUrl() { return pdfUrl; }
    public void setPdfName(String pdfName) { this.pdfName = pdfName; }
    public String getPdfName() { return pdfName; }
}
