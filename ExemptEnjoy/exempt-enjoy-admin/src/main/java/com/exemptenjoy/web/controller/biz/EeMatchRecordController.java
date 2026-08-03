package com.exemptenjoy.web.controller.biz;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import com.exemptenjoy.system.service.IEeAuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.exemptenjoy.common.core.controller.BaseController;
import com.exemptenjoy.common.core.domain.AjaxResult;
import com.exemptenjoy.system.domain.EeMatchRecord;
import com.exemptenjoy.system.service.IEeMatchRecordService;
import com.exemptenjoy.common.core.page.TableDataInfo;
import com.exemptenjoy.common.utils.poi.ExcelUtil;
import com.exemptenjoy.common.utils.SecurityUtils;
import com.exemptenjoy.system.mapper.EeTargetDataMapper;
import com.exemptenjoy.system.domain.EeTargetData;
import java.util.stream.Collectors;

/**
 * 自动匹配及兑现流程Controller
 */
@RestController
@RequestMapping("/biz/matchRecord")
public class EeMatchRecordController extends BaseController {

    @Autowired
    private IEeMatchRecordService eeMatchRecordService;

    @Autowired
    private IEeAuditLogService eeAuditLogService;

    @Autowired
    private EeTargetDataMapper eeTargetDataMapper;

    @Autowired
    private HttpServletRequest request;

    /**
     * 查询自动匹配及兑现流程列表
     */
    @GetMapping("/list")
    public TableDataInfo list(EeMatchRecord eeMatchRecord) {
        startPage();
        List<EeMatchRecord> list = eeMatchRecordService.selectEeMatchRecordList(eeMatchRecord);
        return getDataTable(list);
    }

    /**
     * 获取自动匹配及兑现流程详细信息
     */
    @GetMapping("/{recordId}")
    public AjaxResult getInfo(@PathVariable("recordId") Long recordId) {
        return success(eeMatchRecordService.selectEeMatchRecordByRecordId(recordId));
    }

    /**
     * 新增自动匹配及兑现流程
     */
    @PostMapping
    public AjaxResult add(@RequestBody EeMatchRecord eeMatchRecord) {
        int rows = eeMatchRecordService.insertEeMatchRecord(eeMatchRecord);
        if (rows > 0) {
            eeAuditLogService.log(getUserId(), getUsername(), "新增", "匹配兑现",
                eeMatchRecord.getRecordId(), "新增匹配记录 ID: " + eeMatchRecord.getRecordId(), request.getRemoteAddr());
        }
        return toAjax(rows);
    }

    /**
     * 修改自动匹配及兑现流程
     */
    @PutMapping
    public AjaxResult edit(@RequestBody EeMatchRecord eeMatchRecord) {
        int rows = eeMatchRecordService.updateEeMatchRecord(eeMatchRecord);
        if (rows > 0) {
            eeAuditLogService.log(getUserId(), getUsername(), "修改", "匹配兑现",
                eeMatchRecord.getRecordId(), "修改匹配记录 ID: " + eeMatchRecord.getRecordId(), request.getRemoteAddr());
        }
        return toAjax(rows);
    }

    /**
     * 删除自动匹配及兑现流程
     */
    @DeleteMapping("/{recordIds}")
    public AjaxResult remove(@PathVariable Long[] recordIds) {
        int rows = eeMatchRecordService.deleteEeMatchRecordByRecordIds(recordIds);
        if (rows > 0) {
            eeAuditLogService.log(getUserId(), getUsername(), "删除", "匹配兑现",
                null, "删除匹配记录 ID: " + java.util.Arrays.toString(recordIds), request.getRemoteAddr());
        }
        return toAjax(rows);
    }

    // ==========================================
    // “免申即享”核心流程状态流转 RESTful API
    // ==========================================

    /**
     * 一键执行智能比对匹配
     */
    @PostMapping("/triggerMatch")
    public AjaxResult triggerMatch() {
        int count = eeMatchRecordService.triggerMatch();
        if (count > 0) {
            eeAuditLogService.log(getUserId(), getUsername(), "智能比对", "匹配兑现",
                null, "执行智能比对，共匹配出 " + count + " 条记录", request.getRemoteAddr());
        }
        return success("智能比对计算成功！共匹配出 " + count + " 条符合条件的企业与个人记录。");
    }

    /**
     * 政策找人：精准推送
     */
    @PutMapping("/push/{recordId}")
    public AjaxResult push(@PathVariable("recordId") Long recordId) {
        int rows = eeMatchRecordService.pushPolicy(recordId);
        if (rows > 0) {
            eeAuditLogService.log(getUserId(), getUsername(), "精准推送", "匹配兑现",
                recordId, "推送匹配记录 ID: " + recordId, request.getRemoteAddr());
        }
        return toAjax(rows);
    }

    /**
     * 意愿确认
     */
    @PutMapping("/confirm/{recordId}")
    public AjaxResult confirm(@PathVariable("recordId") Long recordId) {
        int rows = eeMatchRecordService.confirmIntention(recordId);
        if (rows > 0) {
            eeAuditLogService.log(getUserId(), getUsername(), "意愿确认", "匹配兑现",
                recordId, "确认意愿，记录 ID: " + recordId, request.getRemoteAddr());
        }
        return toAjax(rows);
    }

    /**
     * 自动免审拨付兑现
     */
    @PutMapping("/fulfill/{recordId}")
    public AjaxResult fulfill(@PathVariable("recordId") Long recordId) {
        int result = eeMatchRecordService.fulfillPayment(recordId);
        if (result > 0) {
            eeAuditLogService.log(getUserId(), getUsername(), "兑付执行", "匹配兑现",
                recordId, "执行兑付，记录 ID: " + recordId, request.getRemoteAddr());
            return success("资金拨付成功！资金已秒级直达企业/个人银行账户。");
        }
        return error("资金拨付失败，请检查账户或重试。");
    }

    /**
     * 公示归档
     */
    @PutMapping("/archive/{recordId}")
    public AjaxResult archive(@PathVariable("recordId") Long recordId) {
        int rows = eeMatchRecordService.archiveRecord(recordId);
        if (rows > 0) {
            eeAuditLogService.log(getUserId(), getUsername(), "归档", "匹配兑现",
                recordId, "公示归档，记录 ID: " + recordId, request.getRemoteAddr());
        }
        return toAjax(rows);
    }

    /**
     * 批量推送
     */
    @PutMapping("/pushBatch")
    public AjaxResult pushBatch(@RequestBody Long[] recordIds) {
        int count = 0;
        for (Long recordId : recordIds) {
            count += eeMatchRecordService.pushPolicy(recordId);
        }
        if (count > 0) {
            eeAuditLogService.log(getUserId(), getUsername(), "批量推送", "匹配兑现",
                null, "批量推送 " + count + " 条记录，ID: " + java.util.Arrays.toString(recordIds), request.getRemoteAddr());
        }
        return success("批量推送完成，成功推送 " + count + " 条记录。");
    }

    /**
     * 批量归档
     */
    @PutMapping("/archiveBatch")
    public AjaxResult archiveBatch(@RequestBody Long[] recordIds) {
        int count = 0;
        for (Long recordId : recordIds) {
            count += eeMatchRecordService.archiveRecord(recordId);
        }
        if (count > 0) {
            eeAuditLogService.log(getUserId(), getUsername(), "批量归档", "匹配兑现",
                null, "批量归档 " + count + " 条记录，ID: " + java.util.Arrays.toString(recordIds), request.getRemoteAddr());
        }
        return success("批量归档完成，成功归档 " + count + " 条记录。");
    }

    /**
     * 审核操作 (人工审核通过/拒绝)
     */
    @PutMapping("/audit/{recordId}")
    public AjaxResult audit(@PathVariable("recordId") Long recordId, @RequestParam("auditStatus") String auditStatus) {
        int rows = eeMatchRecordService.auditRecord(recordId, auditStatus);
        if (rows > 0) {
            String statusLabel = "2".equals(auditStatus) ? "审核通过" : "0".equals(auditStatus) ? "审核拒绝" : "审核操作";
            eeAuditLogService.log(getUserId(), getUsername(), statusLabel, "匹配兑现",
                recordId, statusLabel + "，记录 ID: " + recordId, request.getRemoteAddr());
        }
        return toAjax(rows);
    }

    @PostMapping("/export")
    public void exportExcel(HttpServletResponse response, EeMatchRecord eeMatchRecord) {
        try {
            List<EeMatchRecord> list = eeMatchRecordService.selectEeMatchRecordList(eeMatchRecord);
            // 非管理员只能导出自己的匹配记录
            if (!SecurityUtils.isAdmin()) {
                Long userId = SecurityUtils.getUserId();
                List<EeTargetData> myTargets = eeTargetDataMapper.selectEeTargetDataByUserId(userId);
                if (myTargets != null && !myTargets.isEmpty()) {
                    List<Long> targetIds = myTargets.stream().map(EeTargetData::getTargetId).collect(Collectors.toList());
                    list = list.stream().filter(r -> targetIds.contains(r.getTargetId())).collect(Collectors.toList());
                } else {
                    list = new ArrayList<>();
                }
            }
            ExcelUtil<EeMatchRecord> util = new ExcelUtil<>(EeMatchRecord.class);
            util.exportExcel(response, list, "匹配兑现记录");
        } catch (Exception e) {
            logger.error("导出Excel失败", e);
        }
    }
}
