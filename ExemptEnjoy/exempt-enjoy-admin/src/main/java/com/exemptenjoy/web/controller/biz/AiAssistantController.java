package com.exemptenjoy.web.controller.biz;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.exemptenjoy.common.config.RuoYiConfig;
import com.exemptenjoy.common.core.domain.AjaxResult;
import com.exemptenjoy.common.utils.StringUtils;
import com.exemptenjoy.system.domain.EePolicyContent;
import com.exemptenjoy.system.mapper.EePolicyContentMapper;


@RestController
public class AiAssistantController {

    @Autowired
    private EePolicyContentMapper contentMapper;

    @PostMapping("/ai/extract/{policyId}")
    public AjaxResult extractPdfText(@PathVariable Long policyId) {
        com.exemptenjoy.system.service.IEePolicyService policyService =
            com.exemptenjoy.common.utils.spring.SpringUtils.getBean(com.exemptenjoy.system.service.IEePolicyService.class);
        com.exemptenjoy.system.domain.EePolicy policy = policyService.selectEePolicyByPolicyId(policyId);
        if (policy == null) {
            return AjaxResult.error("政策不存在");
        }
        extractAndSavePdfText(policyId, policy.getPdfUrl(), contentMapper);
        EePolicyContent result = contentMapper.selectByPolicyId(policyId);
        if (result != null && StringUtils.isNotEmpty(result.getContentText())) {
            return AjaxResult.success("AI学习成功，已掌握「" + policy.getPolicyName() + "」的内容", result);
        }
        return AjaxResult.error("学习失败，请确保政策信息完整（至少填写政策名称、发布部门、备注等）");
    }

    @PostMapping("/ai/ask")
    public AjaxResult ask(@RequestBody Map<String, String> params) {
        String question = params.get("question");
        if (StringUtils.isEmpty(question)) {
            return AjaxResult.error("请输入您的问题");
        }

        List<EePolicyContent> allContents = contentMapper.selectAll();
        if (allContents.isEmpty()) {
            return AjaxResult.success(createResponse("您好！目前政策知识库暂无内容，请管理员先上传政策PDF文件，我会自动学习后为您解答。"));
        }

        String[] questionWords = extractKeywords(question);
        List<Map<String, Object>> scored = new ArrayList<>();

        for (EePolicyContent pc : allContents) {
            if (StringUtils.isEmpty(pc.getContentText())) continue;
            int score = 0;
            String text = pc.getContentText().toLowerCase();
            String name = pc.getPolicyName() != null ? pc.getPolicyName().toLowerCase() : "";
            for (String word : questionWords) {
                if (word.length() < 2) continue;
                String w = word.toLowerCase();
                if (name.contains(w)) score += 10;
                int idx = 0;
                while ((idx = text.indexOf(w, idx)) != -1) {
                    score += 3;
                    idx += w.length();
                }
            }
            if (score > 0) {
                Map<String, Object> item = new HashMap<>();
                item.put("policyId", pc.getPolicyId());
                item.put("policyName", pc.getPolicyName());
                item.put("score", score);
                item.put("contentText", pc.getContentText());
                scored.add(item);
            }
        }

        scored.sort((a, b) -> (int) b.get("score") - (int) a.get("score"));

        if (scored.isEmpty()) {
            return AjaxResult.success(createResponse(
                "抱歉，我在当前政策库中没有找到与您问题相关的内容。\n\n您可以尝试：\n1. 换个关键词提问（如「补贴」「稳岗」「残疾人」等）\n2. 联系管理员上传更多政策文件\n\n目前知识库共有" + allContents.size() + "条政策数据。"
            ));
        }

        StringBuilder answer = new StringBuilder();
        answer.append("根据政策知识库为您找到以下相关信息：\n\n");

        int count = Math.min(scored.size(), 3);
        for (int i = 0; i < count; i++) {
            Map<String, Object> item = scored.get(i);
            String policyName = (String) item.get("policyName");
            String content = (String) item.get("contentText");
            String relevant = extractRelevantParagraph(content, questionWords, 200);
            answer.append("📄 ").append(policyName).append("\n");
            answer.append(relevant).append("\n\n");
        }

        if (scored.size() > 3) {
            answer.append("还有").append(scored.size() - 3).append("条相关政策，如需了解更多请继续提问。");
        }

        return AjaxResult.success(createResponse(answer.toString()));
    }

    @GetMapping("/ai/status")
    public AjaxResult status() {
        List<EePolicyContent> all = contentMapper.selectAll();
        long withContent = all.stream().filter(c -> StringUtils.isNotEmpty(c.getContentText())).count();
        Map<String, Object> info = new HashMap<>();
        info.put("totalPolicies", all.size());
        info.put("learnedPolicies", withContent);
        info.put("ready", withContent > 0);
        return AjaxResult.success(info);
    }

    public static void extractAndSavePdfText(Long policyId, String pdfUrl, EePolicyContentMapper contentMapper) {
        try {
            com.exemptenjoy.system.service.IEePolicyService policyService =
                com.exemptenjoy.common.utils.spring.SpringUtils.getBean(com.exemptenjoy.system.service.IEePolicyService.class);
            com.exemptenjoy.system.domain.EePolicy policy = policyService.selectEePolicyByPolicyId(policyId);
            if (policy == null) return;

            StringBuilder sb = new StringBuilder();
            sb.append("政策名称：").append(policy.getPolicyName()).append("\n");
            sb.append("政策类型：").append("1".equals(policy.getPolicyType()) ? "补贴" : "2".equals(policy.getPolicyType()) ? "奖励" : "减免").append("\n");
            sb.append("补贴金额：").append(policy.getAmount() != null ? policy.getAmount().toPlainString() : "0").append("元\n");
            sb.append("发文单位：").append(policy.getPublishDept() != null ? policy.getPublishDept() : "").append("\n");
            if (policy.getStartDate() != null) sb.append("开始日期：").append(policy.getStartDate()).append("\n");
            if (policy.getEndDate() != null) sb.append("结束日期：").append(policy.getEndDate()).append("\n");
            if (StringUtils.isNotEmpty(policy.getRemark())) {
                sb.append(policy.getRemark()).append("\n");
            }

            String text = sb.toString();
            if (text.trim().length() < 10) return;

            EePolicyContent existing = contentMapper.selectByPolicyId(policyId);
            if (existing != null) {
                existing.setContentText(text.trim());
                existing.setKeywords(extractKeywordsStr(text));
                contentMapper.updateByPolicyId(existing);
            } else {
                EePolicyContent content = new EePolicyContent();
                content.setPolicyId(policyId);
                content.setContentText(text.trim());
                content.setKeywords(extractKeywordsStr(text));
                contentMapper.insert(content);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String extractKeywordsStr(String text) {
        if (StringUtils.isEmpty(text)) return "";
        String[] stops = {"的", "了", "在", "是", "和", "与", "或", "等", "及", "对", "为", "按", "由", "从", "到", "将", "被", "其", "该", "中", "上", "下", "不", "有", "个", "一", "年", "元", "万", "人", "月", "日"};
        String[] words = text.replaceAll("[^\\u4e00-\\u9fa5a-zA-Z0-9\\s]", " ").split("\\s+");
        Map<String, Integer> freq = new HashMap<>();
        for (String w : words) {
            if (w.length() < 2) continue;
            boolean isStop = false;
            for (String s : stops) { if (w.equals(s)) { isStop = true; break; } }
            if (isStop) continue;
            freq.merge(w, 1, Integer::sum);
        }
        return freq.entrySet().stream()
            .sorted((a, b) -> b.getValue() - a.getValue())
            .limit(30)
            .map(Map.Entry::getKey)
            .reduce((a, b) -> a + "," + b)
            .orElse("");
    }

    private String[] extractKeywords(String question) {
        String[] stops = {"的", "了", "在", "是", "和", "与", "或", "等", "及", "对", "为", "按", "由", "从", "到", "将", "被", "其", "该", "中", "上", "下", "不", "有", "个", "一", "什么", "怎么", "如何", "哪些", "可以", "吗", "呢", "啊", "吧", "请", "问"};
        String[] words = question.replaceAll("[^\\u4e00-\\u9fa5a-zA-Z0-9\\s]", " ").split("\\s+");
        List<String> result = new ArrayList<>();
        for (String w : words) {
            if (w.length() < 2) continue;
            boolean isStop = false;
            for (String s : stops) { if (w.equals(s)) { isStop = true; break; } }
            if (!isStop) result.add(w);
        }
        if (result.isEmpty()) {
            for (String w : words) {
                if (w.length() >= 2) result.add(w);
            }
        }
        return result.toArray(new String[0]);
    }

    private String extractRelevantParagraph(String content, String[] keywords, int maxLen) {
        String[] lines = content.split("\\n");
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            if (sb.length() >= maxLen) break;
            String lower = line.toLowerCase();
            boolean match = false;
            for (String kw : keywords) {
                if (kw.length() >= 2 && lower.contains(kw.toLowerCase())) { match = true; break; }
            }
            if (match || sb.length() == 0) {
                if (sb.length() + line.length() + 1 <= maxLen * 2) {
                    if (sb.length() > 0) sb.append("\n");
                    sb.append(line.trim());
                }
            }
        }
        if (sb.length() == 0) {
            sb.append(content.substring(0, Math.min(content.length(), maxLen)));
        }
        String result = sb.toString();
        if (result.length() > maxLen * 2) {
            result = result.substring(0, maxLen * 2) + "...";
        }
        return result;
    }

    private Map<String, Object> createResponse(String answer) {
        Map<String, Object> resp = new HashMap<>();
        resp.put("answer", answer);
        resp.put("timestamp", System.currentTimeMillis());
        return resp;
    }
}