package com.exemptenjoy.system.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.exemptenjoy.system.mapper.EeMatchRecordMapper;
import com.exemptenjoy.system.mapper.EePolicyMapper;
import com.exemptenjoy.system.mapper.EeRuleMapper;
import com.exemptenjoy.system.mapper.EeTargetDataMapper;
import com.exemptenjoy.system.mapper.EeFundMapper;
import com.exemptenjoy.system.domain.EeMatchRecord;
import com.exemptenjoy.system.domain.EePolicy;
import com.exemptenjoy.system.domain.EeRule;
import com.exemptenjoy.system.domain.EeTargetData;
import com.exemptenjoy.system.domain.EeFund;
import com.exemptenjoy.system.service.IEeMatchRecordService;
import com.exemptenjoy.system.service.IEeMessageService;
import com.exemptenjoy.system.service.IEeAuditLogService;
import com.exemptenjoy.system.util.RuleEvaluator;

/**
 * 自动匹配及兑现流程Service业务层处理
 */
@Service
public class EeMatchRecordServiceImpl implements IEeMatchRecordService {

    @Autowired
    private EeMatchRecordMapper eeMatchRecordMapper;

    @Autowired
    private EePolicyMapper eePolicyMapper;

    @Autowired
    private EeRuleMapper eeRuleMapper;

    @Autowired
    private EeTargetDataMapper eeTargetDataMapper;

    @Autowired
    private EeFundMapper eeFundMapper;

    @Autowired
    private IEeMessageService messageService;

    @Autowired
    private IEeAuditLogService auditLogService;

    @Override
    public EeMatchRecord selectEeMatchRecordByRecordId(Long recordId) {
        return eeMatchRecordMapper.selectEeMatchRecordByRecordId(recordId);
    }

    @Override
    public List<EeMatchRecord> selectEeMatchRecordList(EeMatchRecord eeMatchRecord) {
        return eeMatchRecordMapper.selectEeMatchRecordList(eeMatchRecord);
    }

    @Override
    public int insertEeMatchRecord(EeMatchRecord eeMatchRecord) {
        return eeMatchRecordMapper.insertEeMatchRecord(eeMatchRecord);
    }

    @Override
    public int updateEeMatchRecord(EeMatchRecord eeMatchRecord) {
        return eeMatchRecordMapper.updateEeMatchRecord(eeMatchRecord);
    }

    @Override
    public int deleteEeMatchRecordByRecordIds(Long[] recordIds) {
        return eeMatchRecordMapper.deleteEeMatchRecordByRecordIds(recordIds);
    }

    // ==========================================
    // “免申即享”流程闭环管理实现
    // ==========================================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int triggerMatch() {
        // 1. 只清理未确认的旧匹配记录（MATCHED/PUSHED），保留已确认及之后的记录
        List<EeMatchRecord> existing = eeMatchRecordMapper.selectEeMatchRecordList(new EeMatchRecord());
        if (existing != null && !existing.isEmpty()) {
            List<Long> toDelete = existing.stream()
                .filter(r -> "MATCHED".equals(r.getStatus()) && !"9".equals(r.getAuditStatus()))
                .map(EeMatchRecord::getRecordId)
                .collect(java.util.stream.Collectors.toList());
            if (!toDelete.isEmpty()) {
                eeMatchRecordMapper.deleteEeMatchRecordByRecordIds(toDelete.toArray(new Long[0]));
            }
        }

        
        // 收集已保留记录的政策-目标对，避免重复匹配
        List<EeMatchRecord> keptRecords = eeMatchRecordMapper.selectEeMatchRecordList(new EeMatchRecord());
        java.util.Set<String> existingPairs = new java.util.HashSet<>();
        for (EeMatchRecord r : keptRecords) {
            existingPairs.add(r.getPolicyId() + "_" + r.getTargetId());
        }

        // 2. 获取正常的政策、匹配条件与目标画像列表
        EePolicy policyQuery = new EePolicy();
        policyQuery.setStatus("0");
        List<EePolicy> policies = eePolicyMapper.selectEePolicyList(policyQuery);

        EeRule ruleQuery = new EeRule();
        ruleQuery.setStatus("0");
        List<EeRule> rules = eeRuleMapper.selectEeRuleList(ruleQuery);

        EeTargetData targetQuery = new EeTargetData();
        targetQuery.setStatus("0");
        List<EeTargetData> targets = eeTargetDataMapper.selectEeTargetDataList(targetQuery);

        if (policies == null || rules == null || targets == null) {
            return 0;
        }

        int count = 0;

        // 3. 政策匹配对象计算
        for (EeTargetData target : targets) {
            String attrs = target.getAttributes();
            if (attrs == null || attrs.trim().isEmpty()) {
                continue;
            }

            for (EePolicy policy : policies) {
                // 寻找该政策下的第一个匹配规则
                EeRule matchedRule = null;
                for (EeRule rule : rules) {
                    if (rule.getPolicyId() != null && rule.getPolicyId().equals(policy.getPolicyId())) {
                        matchedRule = rule;
                        break;
                    }
                }

                if (matchedRule == null) {
                    continue;
                }

                // 通过轻量级解析引擎评估画像是否匹配
                if (RuleEvaluator.evaluate(matchedRule.getConditionExpr(), attrs)) {
                    // 跳过已有确认/兑付/归档记录的政策-目标对
                    String pairKey = policy.getPolicyId() + "_" + target.getTargetId();
                    if (existingPairs.contains(pairKey)) {
                        continue;
                    }
                    EeMatchRecord record = new EeMatchRecord();
                    record.setPolicyId(policy.getPolicyId());
                    record.setTargetId(target.getTargetId());
                    record.setStatus("MATCHED"); // 已匹配状态
                    record.setFundAmount(policy.getAmount());
                    record.setMatchTime(new Date());

                    // 风控风评与信用分析 (风控引擎)
                    try {
                        JSONObject attrJson = JSON.parseObject(attrs);
                        Integer creditScore = attrJson.getInteger("credit_score");
                        String taxStatus = attrJson.getString("tax_status");

                        if ((creditScore != null && creditScore < 60) || "abnormal".equalsIgnoreCase(taxStatus)) {
                            record.setRiskLevel("2");
                            record.setAuditStatus("1");
                        } else if (creditScore != null && creditScore < 80) {
                            record.setRiskLevel("1");
                            record.setAuditStatus("0");
                        } else {
                            record.setRiskLevel("0");
                            record.setAuditStatus("0");
                        }
                    } catch (Exception e) {
                        record.setRiskLevel("0");
                        record.setAuditStatus("0");
                    }

                    eeMatchRecordMapper.insertEeMatchRecord(record);
                    count++;
                }
            }
        }

        return count;
    }

    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int triggerMatchForTarget(Long targetId) {
        EeTargetData target = eeTargetDataMapper.selectEeTargetDataByTargetId(targetId);
        if (target == null) return 0;
        String attrs = target.getAttributes();
        if (attrs == null || attrs.trim().isEmpty()) return 0;

        // 清理该目标的旧匹配记录（MATCHED/PUSHED状态）
        List<EeMatchRecord> existing = eeMatchRecordMapper.selectEeMatchRecordList(new EeMatchRecord());
        if (existing != null) {
            existing.stream()
                .filter(r -> targetId.equals(r.getTargetId()) && "MATCHED".equals(r.getStatus()))
                .forEach(r -> eeMatchRecordMapper.deleteEeMatchRecordByRecordIds(new Long[]{r.getRecordId()}));
        }

        EePolicy policyQuery = new EePolicy();
        policyQuery.setStatus("0");
        List<EePolicy> policies = eePolicyMapper.selectEePolicyList(policyQuery);

        EeRule ruleQuery = new EeRule();
        ruleQuery.setStatus("0");
        List<EeRule> rules = eeRuleMapper.selectEeRuleList(ruleQuery);

        if (policies == null || rules == null) return 0;

        // 收集已有记录的政策-目标对
        List<EeMatchRecord> keptRecords = eeMatchRecordMapper.selectEeMatchRecordList(new EeMatchRecord());
        java.util.Set<String> existingPairs = new java.util.HashSet<>();
        for (EeMatchRecord r : keptRecords) {
            existingPairs.add(r.getPolicyId() + "_" + r.getTargetId());
        }

        int count = 0;
        for (EePolicy policy : policies) {
            if (existingPairs.contains(policy.getPolicyId() + "_" + targetId)) continue;

            for (EeRule rule : rules) {
                if (!policy.getPolicyId().equals(rule.getPolicyId())) continue;
                if (RuleEvaluator.evaluate(rule.getConditionExpr(), attrs)) {
                    EeMatchRecord record = new EeMatchRecord();
                    record.setPolicyId(policy.getPolicyId());
                    record.setTargetId(targetId);
                    record.setStatus("MATCHED");
                    record.setFundAmount(policy.getAmount());
                    record.setMatchTime(new Date());

                    try {
                        JSONObject attrJson = JSON.parseObject(attrs);
                        Integer creditScore = attrJson.getInteger("credit_score");
                        String taxStatus = attrJson.getString("tax_status");
                        if ((creditScore != null && creditScore < 60) || "abnormal".equalsIgnoreCase(taxStatus)) {
                            record.setRiskLevel("2");
                            record.setAuditStatus("1");
                        } else if (creditScore != null && creditScore < 80) {
                            record.setRiskLevel("1");
                            record.setAuditStatus("0");
                        } else {
                            record.setRiskLevel("0");
                            record.setAuditStatus("0");
                        }
                    } catch (Exception e) {
                        record.setRiskLevel("0");
                        record.setAuditStatus("0");
                    }
                    eeMatchRecordMapper.insertEeMatchRecord(record);
                    count++;
                    break;
                }
            }
        }
        return count;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int pushPolicy(Long recordId) {
        EeMatchRecord record = eeMatchRecordMapper.selectEeMatchRecordByRecordId(recordId);
        if (record != null && "MATCHED".equals(record.getStatus())) {
            record.setStatus("PUSHED");
            if ("9".equals(record.getAuditStatus())) {
                if ("2".equals(record.getRiskLevel())) {
                    record.setAuditStatus("1");
                } else {
                    record.setAuditStatus("0");
                }
            }
            record.setUpdateTime(new Date());
            int result = eeMatchRecordMapper.updateEeMatchRecord(record);
            if (result > 0) {
                EeTargetData target = eeTargetDataMapper.selectEeTargetDataByTargetId(record.getTargetId());
                EePolicy policy = eePolicyMapper.selectEePolicyByPolicyId(record.getPolicyId());
                if (target != null && target.getUserId() != null) {
                    String policyName = policy != null ? policy.getPolicyName() : "政策";
                    String riskHint = "2".equals(record.getRiskLevel()) ? "，该政策需人工审核后确认" : "，请及时确认意愿";
                    messageService.sendNotification(
                        target.getUserId(),
                        "政策推送通知",
                        "您有一条新的政策匹配：【" + policyName + "】，拟兑现金额：" + record.getFundAmount() + "元" + riskHint + "。",
                        "push",
                        recordId,
                        "matchRecord"
                    );
                }
            }
            return result;
        }
        return 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int confirmIntention(Long recordId) {
        EeMatchRecord record = eeMatchRecordMapper.selectEeMatchRecordByRecordId(recordId);
        if (record != null && "PUSHED".equals(record.getStatus())) {
            if ("2".equals(record.getRiskLevel()) && "1".equals(record.getAuditStatus())) {
                return 0;
            }
            if ("3".equals(record.getAuditStatus())) {
                return 0;
            }
            record.setStatus("CONFIRMED");
            record.setUpdateTime(new Date());
            return eeMatchRecordMapper.updateEeMatchRecord(record);
        }
        return 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int fulfillPayment(Long recordId) {
        EeMatchRecord record = eeMatchRecordMapper.selectEeMatchRecordByRecordId(recordId);
        if (record != null && "CONFIRMED".equals(record.getStatus())) {
            EeFund fundQuery = new EeFund();
            fundQuery.setPolicyId(record.getPolicyId());
            List<EeFund> funds = eeFundMapper.selectEeFundList(fundQuery);
            if (funds != null && !funds.isEmpty()) {
                EeFund fund = funds.get(0);
                if (fund.getUsedAmount() == null) {
                    fund.setUsedAmount(java.math.BigDecimal.ZERO);
                }
                java.math.BigDecimal remaining = fund.getTotalBudget().subtract(fund.getUsedAmount());
                if (remaining.compareTo(record.getFundAmount()) < 0) {
                    throw new com.exemptenjoy.common.exception.ServiceException("资金池余额不足，无法兑付！剩余：" + remaining + "元，需兑付：" + record.getFundAmount() + "元");
                }
                fund.setUsedAmount(fund.getUsedAmount().add(record.getFundAmount()));
                fund.setUpdateTime(new Date());
                eeFundMapper.updateEeFund(fund);
            }

            record.setStatus("FULFILLED");
            
            if ("1".equals(record.getAuditStatus())) {
                record.setAuditStatus("2");
            }
            
            record.setUpdateTime(new Date());
            int result = eeMatchRecordMapper.updateEeMatchRecord(record);
            if (result > 0) {
                EeTargetData target = eeTargetDataMapper.selectEeTargetDataByTargetId(record.getTargetId());
                EePolicy policy = eePolicyMapper.selectEePolicyByPolicyId(record.getPolicyId());
                if (target != null && target.getUserId() != null) {
                    String policyName = policy != null ? policy.getPolicyName() : "政策";
                    messageService.sendNotification(
                        target.getUserId(),
                        "资金兑付通知",
                        "您享受的政策【" + policyName + "】已自动兑付，金额：" + record.getFundAmount() + "元已直达您的银行账户。",
                        "fulfill",
                        recordId,
                        "matchRecord"
                    );
                }
            }
            return result;
        }
        return 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int archiveRecord(Long recordId) {
        EeMatchRecord record = eeMatchRecordMapper.selectEeMatchRecordByRecordId(recordId);
        if (record != null && "FULFILLED".equals(record.getStatus())) {
            record.setStatus("ARCHIVED");
            record.setUpdateTime(new Date());
            return eeMatchRecordMapper.updateEeMatchRecord(record);
        }
        return 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int auditRecord(Long recordId, String auditStatus) {
        EeMatchRecord record = eeMatchRecordMapper.selectEeMatchRecordByRecordId(recordId);
        if (record != null && "1".equals(record.getAuditStatus())) {
            record.setAuditStatus(auditStatus);
            record.setUpdateTime(new Date());
            int result = eeMatchRecordMapper.updateEeMatchRecord(record);
            if (result > 0) {
                EeTargetData target = eeTargetDataMapper.selectEeTargetDataByTargetId(record.getTargetId());
                EePolicy policy = eePolicyMapper.selectEePolicyByPolicyId(record.getPolicyId());
                if (target != null && target.getUserId() != null) {
                    String policyName = policy != null ? policy.getPolicyName() : "政策";
                    if ("2".equals(auditStatus)) {
                        messageService.sendNotification(
                            target.getUserId(),
                            "审核通过通知",
                            "您申请的政策【" + policyName + "】已审核通过，请及时确认意愿。",
                            "system",
                            recordId,
                            "matchRecord"
                        );
                    } else if ("3".equals(auditStatus)) {
                        messageService.sendNotification(
                            target.getUserId(),
                            "审核拒绝通知",
                            "您申请的政策【" + policyName + "】审核未通过，如有疑问请联系管理部门。",
                            "system",
                            recordId,
                            "matchRecord"
                        );
                    }
                }
            }
            return result;
        }
        return 0;
    }
}
