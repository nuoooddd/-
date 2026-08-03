package com.exemptenjoy.web.controller.biz;

import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import com.exemptenjoy.common.core.controller.BaseController;
import com.exemptenjoy.common.core.domain.AjaxResult;
import com.exemptenjoy.system.service.ai.ZhipuAiService;
import com.exemptenjoy.system.domain.EePolicy;
import com.exemptenjoy.system.domain.EeRule;
import com.exemptenjoy.system.service.IEePolicyService;
import com.exemptenjoy.system.service.IEeRuleService;

@RestController
@RequestMapping("/biz/ai")
public class EeAiController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(EeAiController.class);

    @Autowired
    private ZhipuAiService aiService;
    @Autowired
    private IEePolicyService policyService;
    @Autowired
    private IEeRuleService ruleService;

    @PostMapping("/chat")
    public AjaxResult chat(@RequestBody Map<String, String> params) {
        String message = params.get("message");
        if (message == null || message.trim().isEmpty()) {
            return error("消息不能为空");
        }
        Long userId = getUserId();
        String systemPrompt = buildSystemPrompt();
        String reply = aiService.chat(userId, message, systemPrompt);
        return success(reply);
    }

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@RequestBody Map<String, String> params) {
        String message = params.get("message");
        SseEmitter emitter = new SseEmitter(180000L);

        if (message == null || message.trim().isEmpty()) {
            try {
                emitter.send(SseEmitter.event().name("error").data("消息不能为空"));
                emitter.complete();
            } catch (Exception ignored) {}
            return emitter;
        }

        Long userId = getUserId();
        String systemPrompt = buildSystemPrompt();

        aiService.chatStream(userId, message, systemPrompt,
            token -> {
                try {
                    emitter.send(SseEmitter.event().name("token").data(token));
                } catch (Exception e) {
                    emitter.completeWithError(e);
                }
            },
            fullReply -> {
                try {
                    emitter.send(SseEmitter.event().name("done").data(fullReply));
                    emitter.complete();
                } catch (Exception e) {
                    emitter.completeWithError(e);
                }
            },
            ex -> {
                try {
                    emitter.send(SseEmitter.event().name("error").data(ex.getMessage() != null ? ex.getMessage() : "AI服务异常"));
                    emitter.complete();
                } catch (Exception ignored) {
                    emitter.completeWithError(ex);
                }
            }
        );

        return emitter;
    }

    @DeleteMapping("/session")
    public AjaxResult clearSession() {
        aiService.clearSession(getUserId());
        return success();
    }

    private String buildSystemPrompt() {
        StringBuilder sb = new StringBuilder();
        sb.append("你是「免申即享」政务便民系统的智能助手。你的职责是：\n");
        sb.append("1. 解答用户关于政策补贴、奖励、减免的咨询\n");
        sb.append("2. 帮助用户了解自己可享受的政策待遇\n");
        sb.append("3. 解释匹配规则、风控等级、审核流程等\n");
        sb.append("4. 指导用户完成意愿确认等操作\n");
        sb.append("5. 回答政务数字化改革相关问题\n\n");
        sb.append("请用简洁友好的中文回答，适当使用emoji增加亲和力。\n\n");

        sb.append("=== 当前系统政策数据 ===\n");
        try {
            List<EePolicy> policies = policyService.selectEePolicyList(new EePolicy());
            if (policies != null) {
                for (EePolicy p : policies) {
                    if ("0".equals(p.getStatus())) {
                        sb.append("- ").append(p.getPolicyName())
                          .append("(类型:").append(p.getPolicyType())
                          .append(", 金额:").append(p.getAmount())
                          .append(", 部门:").append(p.getPublishDept()).append(")\n");
                    }
                }
            }
        } catch (Exception e) { sb.append("(政策数据加载失败)\n"); }

        sb.append("\n=== 当前匹配规则 ===\n");
        try {
            List<EeRule> rules = ruleService.selectEeRuleList(new EeRule());
            if (rules != null) {
                for (EeRule r : rules) {
                    if ("0".equals(r.getStatus())) {
                        sb.append("- ").append(r.getRuleName())
                          .append(": ").append(r.getConditionExpr()).append("\n");
                    }
                }
            }
        } catch (Exception e) { sb.append("(规则数据加载失败)\n"); }

        sb.append("\n=== 匹配流程说明 ===\n");
        sb.append("系统比对 → 精准推送 → 意愿确认 → 自动兑现 → 公示归档\n");
        sb.append("低风险对象走免审绿色通道，中高风险需人工审核。\n");

        return sb.toString();
    }
}
