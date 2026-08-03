package com.exemptenjoy.web.controller.biz;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import com.exemptenjoy.system.service.IEeAuditLogService;
import com.exemptenjoy.common.core.controller.BaseController;
import com.exemptenjoy.common.core.domain.AjaxResult;
import com.exemptenjoy.system.domain.EeRule;
import com.exemptenjoy.system.service.IEeRuleService;
import com.exemptenjoy.common.core.page.TableDataInfo;
import org.apache.commons.lang3.StringEscapeUtils;

@RestController
@RequestMapping("/biz/rule")
public class EeRuleController extends BaseController {
    @Autowired
    private IEeRuleService eeRuleService;

    @Autowired
    private IEeAuditLogService eeAuditLogService;

    @Autowired
    private HttpServletRequest request;

    @GetMapping("/list")
    public TableDataInfo list(EeRule eeRule) {
        startPage();
        List<EeRule> list = eeRuleService.selectEeRuleList(eeRule);
        return getDataTable(list);
    }

    @GetMapping("/{ruleId}")
    public AjaxResult getInfo(@PathVariable("ruleId") Long ruleId) {
        return success(eeRuleService.selectEeRuleByRuleId(ruleId));
    }

    @PostMapping
    public AjaxResult add(@RequestBody EeRule eeRule) {
        if (eeRule.getConditionExpr() != null) {
            eeRule.setConditionExpr(StringEscapeUtils.unescapeHtml4(eeRule.getConditionExpr()));
        }
        int rows = eeRuleService.insertEeRule(eeRule);
        if (rows > 0) {
            eeAuditLogService.log(getUserId(), getUsername(), "新增", "规则管理",
                eeRule.getRuleId(), "新增规则: " + eeRule.getRuleName(), request.getRemoteAddr());
        }
        return toAjax(rows);
    }

    @PutMapping
    public AjaxResult edit(@RequestBody EeRule eeRule) {
        if (eeRule.getConditionExpr() != null) {
            eeRule.setConditionExpr(StringEscapeUtils.unescapeHtml4(eeRule.getConditionExpr()));
        }
        int rows = eeRuleService.updateEeRule(eeRule);
        if (rows > 0) {
            eeAuditLogService.log(getUserId(), getUsername(), "修改", "规则管理",
                eeRule.getRuleId(), "修改规则: " + eeRule.getRuleName(), request.getRemoteAddr());
        }
        return toAjax(rows);
    }

    @DeleteMapping("/{ruleIds}")
    public AjaxResult remove(@PathVariable Long[] ruleIds) {
        int rows = eeRuleService.deleteEeRuleByRuleIds(ruleIds);
        if (rows > 0) {
            eeAuditLogService.log(getUserId(), getUsername(), "删除", "规则管理",
                null, "删除规则 ID: " + java.util.Arrays.toString(ruleIds), request.getRemoteAddr());
        }
        return toAjax(rows);
    }
}
