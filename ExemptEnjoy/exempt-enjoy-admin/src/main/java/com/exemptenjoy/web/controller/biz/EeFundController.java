package com.exemptenjoy.web.controller.biz;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import com.exemptenjoy.system.service.IEeAuditLogService;
import com.exemptenjoy.common.core.controller.BaseController;
import com.exemptenjoy.common.core.domain.AjaxResult;
import com.exemptenjoy.system.domain.EeFund;
import com.exemptenjoy.system.service.IEeFundService;
import com.exemptenjoy.common.core.page.TableDataInfo;

@RestController
@RequestMapping("/biz/fund")
public class EeFundController extends BaseController {
    @Autowired
    private IEeFundService eeFundService;

    @Autowired
    private IEeAuditLogService eeAuditLogService;

    @Autowired
    private HttpServletRequest request;

    @GetMapping("/list")
    public TableDataInfo list(EeFund eeFund) {
        startPage();
        List<EeFund> list = eeFundService.selectEeFundList(eeFund);
        return getDataTable(list);
    }

    @GetMapping("/{fundId}")
    public AjaxResult getInfo(@PathVariable("fundId") Long fundId) {
        return success(eeFundService.selectEeFundByFundId(fundId));
    }

    @PostMapping
    public AjaxResult add(@RequestBody EeFund eeFund) {
        int rows = eeFundService.insertEeFund(eeFund);
        if (rows > 0) {
            eeAuditLogService.log(getUserId(), getUsername(), "新增", "资金管理",
                eeFund.getFundId(), "新增资金: " + eeFund.getPolicyName(), request.getRemoteAddr());
        }
        return toAjax(rows);
    }

    @PutMapping
    public AjaxResult edit(@RequestBody EeFund eeFund) {
        int rows = eeFundService.updateEeFund(eeFund);
        if (rows > 0) {
            eeAuditLogService.log(getUserId(), getUsername(), "修改", "资金管理",
                eeFund.getFundId(), "修改资金: " + eeFund.getPolicyName(), request.getRemoteAddr());
        }
        return toAjax(rows);
    }

    @DeleteMapping("/{fundIds}")
    public AjaxResult remove(@PathVariable Long[] fundIds) {
        int rows = eeFundService.deleteEeFundByFundIds(fundIds);
        if (rows > 0) {
            eeAuditLogService.log(getUserId(), getUsername(), "删除", "资金管理",
                null, "删除资金 ID: " + java.util.Arrays.toString(fundIds), request.getRemoteAddr());
        }
        return toAjax(rows);
    }
}
