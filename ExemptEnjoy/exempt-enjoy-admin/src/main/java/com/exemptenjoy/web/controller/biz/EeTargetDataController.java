package com.exemptenjoy.web.controller.biz;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import com.exemptenjoy.system.service.IEeAuditLogService;
import com.exemptenjoy.common.core.controller.BaseController;
import com.exemptenjoy.common.core.domain.AjaxResult;
import com.exemptenjoy.system.domain.EeTargetData;
import com.exemptenjoy.system.service.IEeTargetDataService;
import com.exemptenjoy.common.core.page.TableDataInfo;

@RestController
@RequestMapping("/biz/targetData")
public class EeTargetDataController extends BaseController {
    @Autowired
    private IEeTargetDataService eeTargetDataService;

    @Autowired
    private IEeAuditLogService eeAuditLogService;

    @Autowired
    private HttpServletRequest request;

    @GetMapping("/list")
    public TableDataInfo list(EeTargetData eeTargetData) {
        startPage();
        List<EeTargetData> list = eeTargetDataService.selectEeTargetDataList(eeTargetData);
        return getDataTable(list);
    }

    @GetMapping("/{targetId}")
    public AjaxResult getInfo(@PathVariable("targetId") Long targetId) {
        return success(eeTargetDataService.selectEeTargetDataByTargetId(targetId));
    }

    @PostMapping
    public AjaxResult add(@RequestBody EeTargetData eeTargetData) {
        int rows = eeTargetDataService.insertEeTargetData(eeTargetData);
        if (rows > 0) {
            eeAuditLogService.log(getUserId(), getUsername(), "新增", "目标数据",
                eeTargetData.getTargetId(), "新增目标: " + eeTargetData.getTargetName(), request.getRemoteAddr());
        }
        return toAjax(rows);
    }

    @PutMapping
    public AjaxResult edit(@RequestBody EeTargetData eeTargetData) {
        int rows = eeTargetDataService.updateEeTargetData(eeTargetData);
        if (rows > 0) {
            eeAuditLogService.log(getUserId(), getUsername(), "修改", "目标数据",
                eeTargetData.getTargetId(), "修改目标: " + eeTargetData.getTargetName(), request.getRemoteAddr());
        }
        return toAjax(rows);
    }

    @DeleteMapping("/{targetIds}")
    public AjaxResult remove(@PathVariable Long[] targetIds) {
        int rows = eeTargetDataService.deleteEeTargetDataByTargetIds(targetIds);
        if (rows > 0) {
            eeAuditLogService.log(getUserId(), getUsername(), "删除", "目标数据",
                null, "删除目标 ID: " + java.util.Arrays.toString(targetIds), request.getRemoteAddr());
        }
        return toAjax(rows);
    }
}
