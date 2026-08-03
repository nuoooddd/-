package com.exemptenjoy.system.domain;

import com.exemptenjoy.common.core.domain.BaseEntity;

/**
 * 目标画像及基础数据实体类 ee_target_data
 */
public class EeTargetData extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 目标ID */
    private Long targetId;

    /** 名称 */
    private String targetName;

    /** 类型 (1企业 2个人) */
    private String targetType;

    /** 证件号 (统一社会信用代码/身份证) */
    private String identifier;

    /** 画像属性 (JSON格式字符串) */
    private String attributes;

    /** 联系电话 */
    private String contactPhone;

    /** 银行账号 */
    private String bankAccount;

    /** 状态 (0正常 1停用) */
    private String status;

    /** 关联系统用户ID */
    private Long userId;

    public void setTargetId(Long targetId) { this.targetId = targetId; }
    public Long getTargetId() { return targetId; }
    
    public void setTargetName(String targetName) { this.targetName = targetName; }
    public String getTargetName() { return targetName; }
    
    public void setTargetType(String targetType) { this.targetType = targetType; }
    public String getTargetType() { return targetType; }
    
    public void setIdentifier(String identifier) { this.identifier = identifier; }
    public String getIdentifier() { return identifier; }
    
    public void setAttributes(String attributes) { this.attributes = attributes; }
    public String getAttributes() { return attributes; }
    
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public String getContactPhone() { return contactPhone; }
    
    public void setBankAccount(String bankAccount) { this.bankAccount = bankAccount; }
    public String getBankAccount() { return bankAccount; }
    
    public void setStatus(String status) { this.status = status; }
    public String getStatus() { return status; }

    public void setUserId(Long userId) { this.userId = userId; }
    public Long getUserId() { return userId; }
}
