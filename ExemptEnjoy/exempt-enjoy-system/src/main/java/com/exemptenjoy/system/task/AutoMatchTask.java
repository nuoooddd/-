package com.exemptenjoy.system.task;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.exemptenjoy.system.domain.EeMatchRecord;
import com.exemptenjoy.system.domain.EeTargetData;
import com.exemptenjoy.system.domain.EePolicy;
import com.exemptenjoy.system.mapper.EeMatchRecordMapper;
import com.exemptenjoy.system.mapper.EeTargetDataMapper;
import com.exemptenjoy.system.mapper.EePolicyMapper;
import com.exemptenjoy.system.service.IEeMatchRecordService;
import com.exemptenjoy.system.service.IEeMessageService;

@Component
public class AutoMatchTask {

    private static final Logger log = LoggerFactory.getLogger(AutoMatchTask.class);

    @Autowired
    private IEeMatchRecordService matchRecordService;

    @Autowired
    private IEeMessageService messageService;

    @Autowired
    private EeMatchRecordMapper matchRecordMapper;

    @Autowired
    private EeTargetDataMapper targetDataMapper;

    @Autowired
    private EePolicyMapper policyMapper;

    @Scheduled(cron = "0 0 2 * * ?")
    public void autoMatchAndPush() {
        log.info("=== 定时自动匹配任务启动 ===");
        try {
            int count = matchRecordService.triggerMatch();
            log.info("智能比对完成，匹配出 {} 条记录", count);

            List<EeMatchRecord> matched = matchRecordMapper.selectEeMatchRecordList(new EeMatchRecord());
            int pushCount = 0;
            for (EeMatchRecord record : matched) {
                if ("MATCHED".equals(record.getStatus())) {
                    matchRecordService.pushPolicy(record.getRecordId());
                    pushCount++;
                }
            }
            log.info("自动推送完成，推送 {} 条", pushCount);
        } catch (Exception e) {
            log.error("定时自动匹配任务异常", e);
        }
    }

    @Scheduled(cron = "0 0 9 * * ?")
    public void checkExpiredPolicies() {
        log.info("=== 政策到期预警检查启动 ===");
        try {
            EePolicy query = new EePolicy();
            query.setStatus("0");
            List<EePolicy> policies = policyMapper.selectEePolicyList(query);
            java.util.Date now = new java.util.Date();
            long sevenDays = 7L * 24 * 60 * 60 * 1000;

            for (EePolicy policy : policies) {
                if (policy.getEndDate() != null) {
                    long endTime = policy.getEndDate().getTime();
                    long diff = endTime - now.getTime();
                    if (diff > 0 && diff < sevenDays) {
                    EeTargetData tQuery = new EeTargetData();
                    tQuery.setStatus("0");
                    List<EeTargetData> targets = targetDataMapper.selectEeTargetDataList(tQuery);
                    for (EeTargetData target : targets) {
                        if (target.getUserId() != null) {
                            messageService.sendNotification(
                                target.getUserId(),
                                "政策即将到期提醒",
                                "政策【" + policy.getPolicyName() + "】即将到期，请尽快确认享受。",
                                "system",
                                policy.getPolicyId(),
                                "policy"
                            );
                        }
                    }
                    log.info("政策【{}】即将到期，已发送预警通知", policy.getPolicyName());
                    }
                }
            }
        } catch (Exception e) {
            log.error("政策到期预警检查异常", e);
        }
    }
}