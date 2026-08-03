package com.exemptenjoy.web.controller.biz;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.exemptenjoy.common.core.controller.BaseController;
import com.exemptenjoy.common.core.domain.AjaxResult;
import com.exemptenjoy.system.domain.EeAuditLog;
import com.exemptenjoy.system.service.IEeAuditLogService;
import com.exemptenjoy.common.core.page.TableDataInfo;

@RestController
@RequestMapping("/biz/auditLog")
public class EeAuditLogController extends BaseController {

    @Autowired
    private IEeAuditLogService eeAuditLogService;

    @GetMapping("/list")
    public TableDataInfo list(EeAuditLog eeAuditLog) {
        startPage();
        List<EeAuditLog> list = eeAuditLogService.selectEeAuditLogList(eeAuditLog);
        return getDataTable(list);
    }

    @GetMapping("/{logId}")
    public AjaxResult getInfo(@PathVariable("logId") Long logId) {
        return success(eeAuditLogService.selectEeAuditLogByLogId(logId));
    }
}