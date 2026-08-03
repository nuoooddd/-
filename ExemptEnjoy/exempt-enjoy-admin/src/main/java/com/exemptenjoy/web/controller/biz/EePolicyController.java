package com.exemptenjoy.web.controller.biz;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import com.exemptenjoy.system.service.IEeAuditLogService;
import com.exemptenjoy.common.core.controller.BaseController;
import com.exemptenjoy.common.core.domain.AjaxResult;
import com.exemptenjoy.common.utils.StringUtils;
import com.exemptenjoy.system.domain.EePolicy;
import com.exemptenjoy.system.service.IEePolicyService;
import com.exemptenjoy.system.mapper.EePolicyContentMapper;
import com.exemptenjoy.common.core.page.TableDataInfo;

@RestController
@RequestMapping("/biz/policy")
public class EePolicyController extends BaseController {
    @Autowired
    private IEePolicyService eePolicyService;

    @Autowired
    private IEeAuditLogService eeAuditLogService;

    @Autowired
    private EePolicyContentMapper contentMapper;

    @Autowired
    private HttpServletRequest request;

    @GetMapping("/list")
    public TableDataInfo list(EePolicy eePolicy) {
        startPage();
        List<EePolicy> list = eePolicyService.selectEePolicyList(eePolicy);
        return getDataTable(list);
    }

    @GetMapping("/{policyId}")
    public AjaxResult getInfo(@PathVariable("policyId") Long policyId) {
        return success(eePolicyService.selectEePolicyByPolicyId(policyId));
    }

    @PostMapping
    public AjaxResult add(@RequestBody EePolicy eePolicy) {
        eePolicy.setCreateBy(getUsername());
        eePolicy.setCreateTime(new Date());
        int rows = eePolicyService.insertEePolicy(eePolicy);
        if (rows > 0) {
            eeAuditLogService.log(getUserId(), getUsername(), "新增", "政策管理",
                eePolicy.getPolicyId(), "新增政策: " + eePolicy.getPolicyName(), request.getRemoteAddr());
            if (StringUtils.isNotEmpty(eePolicy.getPdfUrl())) {
                AiAssistantController.extractAndSavePdfText(eePolicy.getPolicyId(), eePolicy.getPdfUrl(), contentMapper);
            }
        }
        return toAjax(rows);
    }

    @PutMapping
    public AjaxResult edit(@RequestBody EePolicy eePolicy) {
        eePolicy.setUpdateBy(getUsername());
        eePolicy.setUpdateTime(new Date());
        int rows = eePolicyService.updateEePolicy(eePolicy);
        if (rows > 0) {
            eeAuditLogService.log(getUserId(), getUsername(), "修改", "政策管理",
                eePolicy.getPolicyId(), "修改政策: " + eePolicy.getPolicyName(), request.getRemoteAddr());
            if (StringUtils.isNotEmpty(eePolicy.getPdfUrl())) {
                AiAssistantController.extractAndSavePdfText(eePolicy.getPolicyId(), eePolicy.getPdfUrl(), contentMapper);
            }
        }
        return toAjax(rows);
    }

    @DeleteMapping("/{policyIds}")
    public AjaxResult remove(@PathVariable Long[] policyIds) {
        int rows = eePolicyService.deleteEePolicyByPolicyIds(policyIds);
        if (rows > 0) {
            eeAuditLogService.log(getUserId(), getUsername(), "删除", "政策管理",
                null, "删除政策 ID: " + java.util.Arrays.toString(policyIds), request.getRemoteAddr());
        }
        return toAjax(rows);
    }
}
