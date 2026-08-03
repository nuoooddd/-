package com.exemptenjoy.system.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

public class EeAuditRecord
{
    private Long recordId;
    private Long userId;
    private String auditType;
    private String auditStatus;
    private Long auditBy;
    private String auditByName;
    private String auditRemark;
    private String idCardFront;
    private String idCardBack;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date auditTime;
    private String userName;
    private String realName;
    private String phonenumber;

    public Long getRecordId() { return recordId; }
    public void setRecordId(Long recordId) { this.recordId = recordId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getAuditType() { return auditType; }
    public void setAuditType(String auditType) { this.auditType = auditType; }
    public String getAuditStatus() { return auditStatus; }
    public void setAuditStatus(String auditStatus) { this.auditStatus = auditStatus; }
    public Long getAuditBy() { return auditBy; }
    public void setAuditBy(Long auditBy) { this.auditBy = auditBy; }
    public String getAuditByName() { return auditByName; }
    public void setAuditByName(String auditByName) { this.auditByName = auditByName; }
    public String getAuditRemark() { return auditRemark; }
    public void setAuditRemark(String auditRemark) { this.auditRemark = auditRemark; }
    public String getIdCardFront() { return idCardFront; }
    public void setIdCardFront(String idCardFront) { this.idCardFront = idCardFront; }
    public String getIdCardBack() { return idCardBack; }
    public void setIdCardBack(String idCardBack) { this.idCardBack = idCardBack; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public Date getAuditTime() { return auditTime; }
    public void setAuditTime(Date auditTime) { this.auditTime = auditTime; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public String getPhonenumber() { return phonenumber; }
    public void setPhonenumber(String phonenumber) { this.phonenumber = phonenumber; }
}