package com.exemptenjoy.web.controller.biz;

import java.util.*;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.exemptenjoy.common.core.controller.BaseController;
import com.exemptenjoy.common.core.domain.AjaxResult;
import com.exemptenjoy.common.core.page.TableDataInfo;
import com.exemptenjoy.system.domain.EeMatchRecord;
import com.exemptenjoy.system.domain.EePolicy;
import com.exemptenjoy.system.domain.EeTargetData;
import com.exemptenjoy.system.domain.EeFund;
import com.exemptenjoy.system.domain.EeRule;
import com.exemptenjoy.system.domain.vo.MatchDetailVo;
import com.exemptenjoy.system.domain.vo.RuleDetailVo;
import com.exemptenjoy.system.domain.vo.ConditionDetailVo;
import com.exemptenjoy.system.service.IEeMatchRecordService;
import com.exemptenjoy.system.service.IEePolicyService;
import com.exemptenjoy.system.service.IEeTargetDataService;
import com.exemptenjoy.system.service.IEeFundService;
import com.exemptenjoy.system.service.IEeRuleService;
import com.exemptenjoy.system.mapper.EeMatchRecordMapper;
import com.exemptenjoy.system.mapper.EeTargetDataMapper;
import com.exemptenjoy.system.mapper.EeRuleMapper;

import com.exemptenjoy.system.domain.EeMessage;
import com.exemptenjoy.system.service.IEeMessageService;
import com.exemptenjoy.system.mapper.EeMessageMapper;
import com.exemptenjoy.common.core.domain.entity.SysUser;
import com.exemptenjoy.system.mapper.SysUserMapper;
import com.exemptenjoy.system.util.RuleEvaluator;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

@RestController
@RequestMapping("/biz/user")
public class EeUserController extends BaseController {

    @Autowired
    private IEeMatchRecordService matchRecordService;
    @Autowired
    private IEePolicyService policyService;
    @Autowired
    private IEeTargetDataService targetDataService;
    @Autowired
    private IEeFundService fundService;
    @Autowired
    private IEeRuleService ruleService;
    @Autowired
    private EeRuleMapper ruleMapper;
    @Autowired
    private EeMatchRecordMapper matchRecordMapper;
    @Autowired
    private EeTargetDataMapper targetDataMapper;

    @Autowired
    private IEeMessageService messageService;

    @Autowired
    private SysUserMapper sysUserMapper;


    @GetMapping("/myRecords")
    public TableDataInfo myRecords() {
        startPage();
        Long userId = getUserId();
        List<EeTargetData> myTargets = targetDataMapper.selectEeTargetDataByUserId(userId);
        if (myTargets == null || myTargets.isEmpty()) {
            return getDataTable(new ArrayList<>());
        }
        List<Long> targetIds = myTargets.stream().map(EeTargetData::getTargetId).collect(Collectors.toList());
        List<EeMatchRecord> allRecords = matchRecordMapper.selectEeMatchRecordList(new EeMatchRecord());
        List<EeMatchRecord> myRecords = allRecords.stream()
            .filter(r -> targetIds.contains(r.getTargetId()))
            .filter(r -> !"MATCHED".equals(r.getStatus()))
            .collect(Collectors.toList());
        return getDataTable(myRecords);
    }

    @GetMapping("/myProfile")
    public AjaxResult myProfile() {
        Long userId = getUserId();
        List<EeTargetData> myTargets = targetDataMapper.selectEeTargetDataByUserId(userId);
        if (myTargets != null && !myTargets.isEmpty()) {
            return success(myTargets.get(0));
        }
        return success();
    }

    @GetMapping("/myPolicies")
    public AjaxResult myPolicies() {
        Long userId = getUserId();
        List<EeTargetData> myTargets = targetDataMapper.selectEeTargetDataByUserId(userId);
        if (myTargets == null || myTargets.isEmpty()) {
            return success(new ArrayList<>());
        }
        List<Long> targetIds = myTargets.stream().map(EeTargetData::getTargetId).collect(Collectors.toList());
        List<EeMatchRecord> allRecords = matchRecordMapper.selectEeMatchRecordList(new EeMatchRecord());
        List<Long> policyIds = allRecords.stream()
            .filter(r -> targetIds.contains(r.getTargetId()))
            .map(EeMatchRecord::getPolicyId)
            .distinct()
            .collect(Collectors.toList());
        List<EePolicy> policies = new ArrayList<>();
        for (Long policyId : policyIds) {
            EePolicy p = policyService.selectEePolicyByPolicyId(policyId);
            if (p != null) policies.add(p);
        }
        return success(policies);
    }

    @GetMapping("/dashboard")
    public AjaxResult dashboard() {
        Long userId = getUserId();
        Map<String, Object> data = new HashMap<>();
        List<EeTargetData> myTargets = targetDataMapper.selectEeTargetDataByUserId(userId);
        int myTargetCount = (myTargets == null) ? 0 : myTargets.size();
        data.put("myTargetCount", myTargetCount);

        if (myTargets != null && !myTargets.isEmpty()) {
            List<Long> targetIds = myTargets.stream().map(EeTargetData::getTargetId).collect(Collectors.toList());
            List<EeMatchRecord> allRecords = matchRecordMapper.selectEeMatchRecordList(new EeMatchRecord());
            List<EeMatchRecord> myRecords = allRecords.stream()
                .filter(r -> targetIds.contains(r.getTargetId()))
                .collect(Collectors.toList());
            data.put("matchedCount", myRecords.size());
            data.put("pushedCount", myRecords.stream().filter(r -> "PUSHED".equals(r.getStatus())).count());
            data.put("confirmedCount", myRecords.stream().filter(r -> "CONFIRMED".equals(r.getStatus())).count());
            data.put("fulfilledCount", myRecords.stream().filter(r -> "FULFILLED".equals(r.getStatus()) || "ARCHIVED".equals(r.getStatus())).count());
            data.put("archivedCount", myRecords.stream().filter(r -> "ARCHIVED".equals(r.getStatus())).count());
            double totalAmount = myRecords.stream()
                .filter(r -> "FULFILLED".equals(r.getStatus()) || "ARCHIVED".equals(r.getStatus()))
                .mapToDouble(r -> r.getFundAmount() == null ? 0 : r.getFundAmount().doubleValue())
                .sum();
            data.put("fulfilledAmount", totalAmount);
            long unreadCount = myRecords.stream().filter(r -> "PUSHED".equals(r.getStatus())).count();
            data.put("unreadCount", unreadCount);
        } else {
            data.put("matchedCount", 0);
            data.put("pushedCount", 0);
            data.put("confirmedCount", 0);
            data.put("fulfilledCount", 0);
            data.put("archivedCount", 0);
            data.put("fulfilledAmount", 0);
            data.put("unreadCount", 0);
        }
        return success(data);
    }

    @PutMapping("/confirm/{recordId}")
    public AjaxResult confirm(@PathVariable("recordId") Long recordId) {
        return toAjax(matchRecordService.confirmIntention(recordId));
    }

    @PutMapping("/updateProfile")
    public AjaxResult updateProfile(@RequestBody EeTargetData targetData) {
        Long userId = getUserId();
        List<EeTargetData> myTargets = targetDataMapper.selectEeTargetDataByUserId(userId);
        if (myTargets == null || myTargets.isEmpty()) {
            // 首次补全画像：自动创建目标数据记录
            EeTargetData newTarget = new EeTargetData();
            newTarget.setUserId(userId);
            // 从当前登录用户获取名称
            newTarget.setTargetName(getLoginUser().getUser().getNickName());
            String reqType = targetData.getTargetType();
            newTarget.setTargetType(reqType != null && !reqType.isEmpty() ? reqType : "1"); // 以用户选择为准，默认企业
            newTarget.setStatus("0");
            newTarget.setIdentifier("-");
            if (targetData.getContactPhone() != null) {
                newTarget.setContactPhone(targetData.getContactPhone());
            }
            if (targetData.getBankAccount() != null) {
                newTarget.setBankAccount(targetData.getBankAccount());
            }
            if (targetData.getAttributes() != null) {
                newTarget.setAttributes(targetData.getAttributes());
            }
            targetDataService.insertEeTargetData(newTarget);
            // 自动触发个人匹配
            matchRecordService.triggerMatchForTarget(newTarget.getTargetId());
            return success("画像已保存，系统正在为您匹配合适的政策...");
        }
        EeTargetData existing = myTargets.get(0);
        if (targetData.getContactPhone() != null) {
            existing.setContactPhone(targetData.getContactPhone());
        }
        if (targetData.getBankAccount() != null) {
            existing.setBankAccount(targetData.getBankAccount());
        }
        if (targetData.getAttributes() != null) {
            existing.setAttributes(targetData.getAttributes());
        }
        targetDataService.updateEeTargetData(existing);
            // 自动触发个人匹配
            matchRecordService.triggerMatchForTarget(existing.getTargetId());
            return success("画像已保存，系统正在为您匹配合适的政策...");
    }

    @GetMapping("/allPolicies")
    public AjaxResult allPolicies() {
        List<EePolicy> policies = policyService.selectEePolicyList(new EePolicy());
        return success(policies);
    }

    @GetMapping("/allTargets")
    public AjaxResult allTargets() {
        List<EeTargetData> targets = targetDataService.selectEeTargetDataList(new EeTargetData());
        return success(targets);
    }

    @GetMapping("/matchDetails")
    public AjaxResult matchDetails() {
        Long userId = getUserId();
        // 1. 获取当前用户的企业
        List<EeTargetData> myTargets = targetDataMapper.selectEeTargetDataByUserId(userId);
        if (myTargets == null || myTargets.isEmpty()) {
            return success(new ArrayList<>());
        }
        EeTargetData target = myTargets.get(0);
        String attrsJson = target.getAttributes();
        JSONObject attrs = (attrsJson != null && !attrsJson.isEmpty())
            ? JSON.parseObject(attrsJson) : new JSONObject();

        // 2. 获取所有启用的政策
        EePolicy policyQuery = new EePolicy();
        policyQuery.setStatus("0");
        List<EePolicy> allPolicies = policyService.selectEePolicyList(policyQuery);

        // 3. 获取当前企业的匹配记录
        List<EeMatchRecord> allRecords = matchRecordMapper.selectEeMatchRecordList(new EeMatchRecord());
        Map<Long, EeMatchRecord> matchedMap = allRecords.stream()
            .filter(r -> target.getTargetId().equals(r.getTargetId()))
            .collect(Collectors.toMap(EeMatchRecord::getPolicyId, r -> r, (a,b) -> a));

        // 4. 获取所有规则
        EeRule ruleQuery = new EeRule();
        ruleQuery.setStatus("0");
        List<EeRule> allRules = ruleMapper.selectEeRuleList(ruleQuery);
        Map<Long, List<EeRule>> rulesByPolicy = allRules.stream()
            .filter(r -> r.getPolicyId() != null)
            .collect(Collectors.groupingBy(EeRule::getPolicyId));

        // 5. 构造结果
        List<MatchDetailVo> result = new ArrayList<>();
        for (EePolicy policy : allPolicies) {
            MatchDetailVo vo = new MatchDetailVo();
            vo.setPolicyId(policy.getPolicyId());
            vo.setPolicyName(policy.getPolicyName());
            vo.setPolicyType(policy.getPolicyType());
            vo.setAmount(policy.getAmount());
            vo.setPublishDept(policy.getPublishDept());
            vo.setRemark(policy.getRemark());

            EeMatchRecord record = matchedMap.get(policy.getPolicyId());
            vo.setMatched(false); // will be updated after rule evaluation
            if (record != null) {
                vo.setRecordId(record.getRecordId());
                vo.setRecordStatus(record.getStatus());
                vo.setAuditStatus(record.getAuditStatus());
                vo.setFundAmount(record.getFundAmount());
            }

            // 解析该政策下的所有规则
            List<EeRule> policyRules = rulesByPolicy.getOrDefault(policy.getPolicyId(), Collections.emptyList());
            List<RuleDetailVo> ruleDetails = new ArrayList<>();
            for (EeRule rule : policyRules) {
                RuleDetailVo rd = new RuleDetailVo();
                rd.setRuleId(rule.getRuleId());
                rd.setRuleName(rule.getRuleName());
                rd.setConditionExpr(rule.getConditionExpr());

                List<ConditionDetailVo> conditions = parseAndEvaluateConditions(rule.getConditionExpr(), attrs);
                rd.setConditions(conditions);
                rd.setPassed(conditions.stream().allMatch(ConditionDetailVo::isPassed));
                ruleDetails.add(rd);
            }
            vo.setRules(ruleDetails);
            boolean allRulesPassed = !ruleDetails.isEmpty() && ruleDetails.stream().allMatch(RuleDetailVo::isPassed);
            vo.setMatched(record != null || allRulesPassed);
            result.add(vo);
        }

        return success(result);
    }

    /**
    /**
     * 解析条件表达式并逐条评估
     * 支持 OR 和 AND 逻辑，正确展示所有条件分支
     */
    private List<ConditionDetailVo> parseAndEvaluateConditions(String conditionExpr, JSONObject attrs) {
        List<ConditionDetailVo> result = new ArrayList<>();
        if (conditionExpr == null || conditionExpr.trim().isEmpty()) {
            return result;
        }
        String expr = conditionExpr.trim();
        String[] orParts = expr.split("(?i)\\s+or\\s+");
        boolean wholeOrPassed = false;
        if (orParts.length > 1) {
            wholeOrPassed = RuleEvaluator.evaluate(expr, attrs.toJSONString());
        }
        String targetPart = orParts[0].trim();
        String[] andParts = targetPart.split("(?i)\\s+and\\s+");
        for (String part : andParts) {
            part = part.trim();
            if (part.isEmpty()) continue;
            ConditionDetailVo cd = parseSingleCondition(part, attrs);
            if (cd != null) result.add(cd);
        }
        if (orParts.length > 1) {
            for (int i = 1; i < orParts.length; i++) {
                String orBranch = orParts[i].trim();
                if (orBranch.isEmpty()) continue;
                String[] orAndParts = orBranch.split("(?i)\\s+and\\s+");
                for (String part : orAndParts) {
                    part = part.trim();
                    if (part.isEmpty()) continue;
                    ConditionDetailVo cd = parseSingleCondition(part, attrs);
                    if (cd != null) {
                        if (wholeOrPassed && !cd.isPassed()) {
                            cd.setPassed(true);
                            cd.setOperator(cd.getOperator() + "(或)");
                        }
                        result.add(cd);
                    }
                }
            }
        }
        return result;
    }

    private ConditionDetailVo parseSingleCondition(String part, JSONObject attrs) {
        ConditionDetailVo cd = new ConditionDetailVo();
        cd.setExpression(part);
        String regex = "(.*?)(>=|<=|==|!=|>|<)(.*)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(part);
        if (matcher.matches()) {
            String field = matcher.group(1).trim();
            String op = matcher.group(2).trim();
            String expected = cleanQuote(matcher.group(3).trim());
            cd.setField(field);
            cd.setOperator(op);
            cd.setExpectedValue(expected);
            Object actualObj = attrs.get(field);
            String actualStr = (actualObj != null) ? String.valueOf(actualObj).trim() : null;
            cd.setActualValue(actualStr);
            if (actualStr == null) {
                cd.setPassed(false);
            } else {
                String singleExpr = field + " " + op + " " + matcher.group(3).trim();
                JSONObject singleAttr = new JSONObject();
                singleAttr.put(field, actualObj);
                cd.setPassed(RuleEvaluator.evaluate(singleExpr, singleAttr.toJSONString()));
            }
        } else {
            cd.setPassed(false);
        }
        return cd;
    }


    /**
     * 企业端主动申请推送 - 通知管理员处理
     */
    @PutMapping("/apply/{recordId}")
    public AjaxResult applyForPush(@PathVariable Long recordId) {
        Long userId = getUserId();
        EeMatchRecord record = matchRecordService.selectEeMatchRecordByRecordId(recordId);
        if (record == null) {
            return error("记录不存在");
        }
        List<EeTargetData> myTargets = targetDataMapper.selectEeTargetDataByUserId(userId);
        boolean isMine = myTargets != null && myTargets.stream()
            .anyMatch(t -> t.getTargetId().equals(record.getTargetId()));
        if (!isMine) {
            return error("无权操作此记录");
        }
        if (!"MATCHED".equals(record.getStatus())) {
            return error("当前状态不可申请推送");
        }
        if ("9".equals(record.getAuditStatus())) {
            return error("已申请过，请等待管理员处理");
        }
        record.setAuditStatus("9"); // 9=申请中
        matchRecordService.updateEeMatchRecord(record);

        String targetName = "";
        try {
            EeTargetData target = targetDataService.selectEeTargetDataByTargetId(record.getTargetId());
            if (target != null) targetName = target.getTargetName();
        } catch (Exception ignored) {}

        String policyName = "";
        try {
            EePolicy policy = policyService.selectEePolicyByPolicyId(record.getPolicyId());
            if (policy != null) policyName = policy.getPolicyName();
        } catch (Exception ignored) {}

        // 通知管理员（user_id=1）
        messageService.sendNotification(
            1L,
            "企业端申请推送",
            targetName + " 申请了政策「" + policyName + "」，请及时处理推送。",
            "APPLY",
            recordId,
            "MATCH_RECORD"
        );
        return success("申请已提交，管理员将尽快处理");
    }
    /**
     * 用户主动申请政策，支持上传佐证材料
     */
    @PostMapping("/applyPolicy")
    public AjaxResult applyPolicy(@RequestBody Map<String, Object> params)
    {
        Long userId = getUserId();
        Long policyId = Long.valueOf(params.get("policyId").toString());
        String evidenceUrl = params.get("evidenceUrl") != null ? params.get("evidenceUrl").toString() : null;
        String evidenceName = params.get("evidenceName") != null ? params.get("evidenceName").toString() : null;
        String applyRemark = params.get("applyRemark") != null ? params.get("applyRemark").toString() : null;

        EePolicy policy = policyService.selectEePolicyByPolicyId(policyId);
        if (policy == null)
        {
            return error("政策不存在");
        }

        List<EeTargetData> myTargets = targetDataMapper.selectEeTargetDataByUserId(userId);
        EeTargetData target;
        if (myTargets == null || myTargets.isEmpty())
        {
            target = new EeTargetData();
            target.setUserId(userId);
            target.setTargetName(getLoginUser().getUser().getNickName());
            target.setTargetType("1");
            target.setStatus("0");
            target.setIdentifier("-");
            targetDataService.insertEeTargetData(target);
        }
        else
        {
            target = myTargets.get(0);
        }

        List<EeMatchRecord> allRecords = matchRecordMapper.selectEeMatchRecordList(new EeMatchRecord());
        boolean exists = allRecords.stream()
            .anyMatch(r -> target.getTargetId().equals(r.getTargetId()) && policyId.equals(r.getPolicyId()));
        if (exists)
        {
            return error("您已申请过该政策，请勿重复申请");
        }

        EeMatchRecord record = new EeMatchRecord();
        record.setPolicyId(policyId);
        record.setTargetId(target.getTargetId());
        record.setFundAmount(policy.getAmount());
        record.setStatus("MATCHED");
        record.setAuditStatus("9");
        record.setRiskLevel("1");
        record.setRemark(applyRemark);
        if (evidenceUrl != null)
        {
            record.setProofFile(evidenceUrl);
        }
        matchRecordService.insertEeMatchRecord(record);

        messageService.sendNotification(
            1L,
            "用户申请政策",
            target.getTargetName() + " 申请了政策「" + policy.getPolicyName() + "」" + (evidenceName != null ? "，已上传佐证材料" : "") + "，请及时审核。",
            "APPLY",
            record.getRecordId(),
            "MATCH_RECORD"
        );
        return success("申请已提交，管理员将尽快审核");
    }

    private String cleanQuote(String str) {
        if (str == null || str.isEmpty()) return "";
        str = str.trim();
        if ((str.startsWith("'") && str.endsWith("'")) || (str.startsWith("\"") && str.endsWith("\""))) {
            return str.substring(1, str.length() - 1);
        }
        return str;
    }

}
