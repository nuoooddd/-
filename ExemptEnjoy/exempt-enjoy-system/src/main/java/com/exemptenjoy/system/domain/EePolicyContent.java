package com.exemptenjoy.system.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

public class EePolicyContent {
    private Long contentId;
    private Long policyId;
    private String contentText;
    private String keywords;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
    private String policyName;

    public void setContentId(Long contentId) { this.contentId = contentId; }
    public Long getContentId() { return contentId; }
    public void setPolicyId(Long policyId) { this.policyId = policyId; }
    public Long getPolicyId() { return policyId; }
    public void setContentText(String contentText) { this.contentText = contentText; }
    public String getContentText() { return contentText; }
    public void setKeywords(String keywords) { this.keywords = keywords; }
    public String getKeywords() { return keywords; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public Date getCreateTime() { return createTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
    public Date getUpdateTime() { return updateTime; }
    public void setPolicyName(String policyName) { this.policyName = policyName; }
    public String getPolicyName() { return policyName; }
}