package com.exemptenjoy.system.domain;

import com.exemptenjoy.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;

public class EeAuditLog extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long logId;
    private Long userId;
    private String userName;
    private String operation;
    private String module;
    private Long targetId;
    private String detail;
    private String ip;

    public void setLogId(Long logId) { this.logId = logId; }
    public Long getLogId() { return logId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getUserId() { return userId; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getUserName() { return userName; }
    public void setOperation(String operation) { this.operation = operation; }
    public String getOperation() { return operation; }
    public void setModule(String module) { this.module = module; }
    public String getModule() { return module; }
    public void setTargetId(Long targetId) { this.targetId = targetId; }
    public Long getTargetId() { return targetId; }
    public void setDetail(String detail) { this.detail = detail; }
    public String getDetail() { return detail; }
    public void setIp(String ip) { this.ip = ip; }
    public String getIp() { return ip; }
}