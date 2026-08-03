package com.exemptenjoy.system.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

public class EeVerifyRecord
{
    private Long recordId;
    private Long userId;
    private String verifyType;
    private String verifyResult;
    private String verifyContent;
    private String verifyNo;
    private String ipAddress;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    public Long getRecordId()
    {
        return recordId;
    }

    public void setRecordId(Long recordId)
    {
        this.recordId = recordId;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public String getVerifyType()
    {
        return verifyType;
    }

    public void setVerifyType(String verifyType)
    {
        this.verifyType = verifyType;
    }

    public String getVerifyResult()
    {
        return verifyResult;
    }

    public void setVerifyResult(String verifyResult)
    {
        this.verifyResult = verifyResult;
    }

    public String getVerifyContent()
    {
        return verifyContent;
    }

    public void setVerifyContent(String verifyContent)
    {
        this.verifyContent = verifyContent;
    }

    public String getVerifyNo()
    {
        return verifyNo;
    }

    public void setVerifyNo(String verifyNo)
    {
        this.verifyNo = verifyNo;
    }

    public String getIpAddress()
    {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress)
    {
        this.ipAddress = ipAddress;
    }

    public Date getCreateTime()
    {
        return createTime;
    }

    public void setCreateTime(Date createTime)
    {
        this.createTime = createTime;
    }
}