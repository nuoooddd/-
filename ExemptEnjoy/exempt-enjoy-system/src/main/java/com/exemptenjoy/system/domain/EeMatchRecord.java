package com.exemptenjoy.system.domain;

import com.exemptenjoy.common.core.domain.BaseEntity;
import com.exemptenjoy.common.annotation.Excel;
import com.exemptenjoy.common.annotation.Excel.ColumnType;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 自动匹配及兑现流程实体类 ee_match_record
 */
public class EeMatchRecord extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 记录ID */
    @Excel(name = "记录编号", cellType = ColumnType.NUMERIC)
    private Long recordId;

    /** 政策ID */
    @Excel(name = "政策ID", cellType = ColumnType.NUMERIC)
    private Long policyId;

    /** 目标ID */
    @Excel(name = "目标ID", cellType = ColumnType.NUMERIC)
    private Long targetId;

    /** 自动比对时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "匹配时间", dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date matchTime;

    /** 流程状态(MATCHED:已匹配, PUSHED:已推送, CONFIRMED:意愿已确认, FULFILLED:自动已兑现, ARCHIVED:已公示归档) */
    @Excel(name = "流程状态", readConverterExp = "MATCHED=已匹配,PUSHED=已推送,CONFIRMED=已确认,FULFILLED=已兑付,ARCHIVED=已归档")
    private String status;

    /** 拟兑现金额 */
    @Excel(name = "拟兑付金额", cellType = ColumnType.NUMERIC)
    private BigDecimal fundAmount;

    /** 审核状态(0免审 1人工审核中 2审核通过 3审核拒绝) */
    @Excel(name = "审核状态", readConverterExp = "0=免审,1=人工审核,2=审核通过,3=审核拒绝")
    private String auditStatus;

    /** 风控等级(0低风险 1中风险 2高风险) */
    @Excel(name = "风控等级", readConverterExp = "0=低风险,1=中风险,2=高风险")
    private String riskLevel;

    /** 佐证材料PDF路径 */
    private String proofFile;

    // ==========================================
    // VO 关联字段，用于前端大盘和表格完美渲染，不映射数据库字段
    // ==========================================
    
    /** 关联政策名称 */
    @Excel(name = "政策名称")
    private String policyName;

    /** 关联目标对象名称 */
    @Excel(name = "目标对象")
    private String targetName;

    /** 目标对象证件号码 */
    @Excel(name = "证件号码")
    private String identifier;

    public void setRecordId(Long recordId) { this.recordId = recordId; }
    public Long getRecordId() { return recordId; }
    
    public void setPolicyId(Long policyId) { this.policyId = policyId; }
    public Long getPolicyId() { return policyId; }
    
    public void setTargetId(Long targetId) { this.targetId = targetId; }
    public Long getTargetId() { return targetId; }
    
    public void setMatchTime(Date matchTime) { this.matchTime = matchTime; }
    public Date getMatchTime() { return matchTime; }
    
    public void setStatus(String status) { this.status = status; }
    public String getStatus() { return status; }
    
    public void setFundAmount(BigDecimal fundAmount) { this.fundAmount = fundAmount; }
    public BigDecimal getFundAmount() { return fundAmount; }
    
    public void setAuditStatus(String auditStatus) { this.auditStatus = auditStatus; }
    public String getAuditStatus() { return auditStatus; }
    
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public String getRiskLevel() { return riskLevel; }

    public void setProofFile(String proofFile) { this.proofFile = proofFile; }
    public String getProofFile() { return proofFile; }

    public void setPolicyName(String policyName) { this.policyName = policyName; }
    public String getPolicyName() { return policyName; }

    public void setTargetName(String targetName) { this.targetName = targetName; }
    public String getTargetName() { return targetName; }

    public void setIdentifier(String identifier) { this.identifier = identifier; }
    public String getIdentifier() { return identifier; }

    /** 关联政策PDF路径 */
    private String pdfUrl;

    /** 关联政策PDF名称 */
    private String pdfName;

    public void setPdfUrl(String pdfUrl) { this.pdfUrl = pdfUrl; }
    public String getPdfUrl() { return pdfUrl; }
    public void setPdfName(String pdfName) { this.pdfName = pdfName; }
    public String getPdfName() { return pdfName; }
}

