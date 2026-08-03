package com.exemptenjoy.web.controller.biz;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.exemptenjoy.common.core.controller.BaseController;
import com.exemptenjoy.common.core.domain.AjaxResult;
import com.exemptenjoy.common.core.page.TableDataInfo;
import com.exemptenjoy.common.utils.SecurityUtils;
import com.exemptenjoy.system.domain.EeAuditRecord;
import com.exemptenjoy.system.domain.EeVerifyRecord;
import com.exemptenjoy.system.mapper.EeAuditRecordMapper;
import com.exemptenjoy.system.mapper.EeVerifyRecordMapper;
import com.exemptenjoy.system.service.ISysUserService;
import com.exemptenjoy.common.core.domain.entity.SysUser;

@RestController
@RequestMapping("/biz/audit")
public class EeAuditController extends BaseController
{
    @Autowired
    private EeAuditRecordMapper auditRecordMapper;

    @Autowired
    private EeVerifyRecordMapper verifyRecordMapper;

    @Autowired
    private ISysUserService userService;

    @PreAuthorize("@ss.hasPermi('biz:audit:list')")
    @GetMapping("/list")
    public TableDataInfo list(EeAuditRecord record)
    {
        startPage();
        List<EeAuditRecord> list = auditRecordMapper.selectAuditList(record);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('biz:audit:query')")
    @GetMapping("/{recordId}")
    public AjaxResult getInfo(@PathVariable Long recordId)
    {
        return success(auditRecordMapper.selectAuditById(recordId));
    }

    @PreAuthorize("@ss.hasPermi('biz:audit:approve')")
    @PutMapping("/approve")
    public AjaxResult approve(@RequestBody EeAuditRecord record)
    {
        record.setAuditBy(SecurityUtils.getUserId());
        record.setAuditByName(SecurityUtils.getUsername());
        record.setAuditStatus("1");
        auditRecordMapper.updateAuditStatus(record);

        EeAuditRecord auditRecord = auditRecordMapper.selectAuditById(record.getRecordId());
        if (auditRecord != null && auditRecord.getUserId() != null)
        {
            SysUser user = new SysUser();
            user.setUserId(auditRecord.getUserId());
            user.setAuditStatus("1");
            user.setAuditRemark(record.getAuditRemark());
            user.setAuditTime(new java.util.Date());
            user.setAuditBy(SecurityUtils.getUsername());
            user.setIdCardVerified("1");
            user.setVerifyTime(new java.util.Date());
            userService.updateUser(user);

            EeVerifyRecord verifyRecord = new EeVerifyRecord();
            verifyRecord.setUserId(auditRecord.getUserId());
            verifyRecord.setVerifyType("idcard");
            verifyRecord.setVerifyResult("1");
            verifyRecord.setVerifyContent("身份证审核通过");
            verifyRecordMapper.insertEeVerifyRecord(verifyRecord);
        }
        return success();
    }

    @PreAuthorize("@ss.hasPermi('biz:audit:approve')")
    @PutMapping("/reject")
    public AjaxResult reject(@RequestBody EeAuditRecord record)
    {
        record.setAuditBy(SecurityUtils.getUserId());
        record.setAuditByName(SecurityUtils.getUsername());
        record.setAuditStatus("2");
        auditRecordMapper.updateAuditStatus(record);

        EeAuditRecord auditRecord = auditRecordMapper.selectAuditById(record.getRecordId());
        if (auditRecord != null && auditRecord.getUserId() != null)
        {
            SysUser user = new SysUser();
            user.setUserId(auditRecord.getUserId());
            user.setAuditStatus("2");
            user.setAuditRemark(record.getAuditRemark());
            user.setAuditTime(new java.util.Date());
            user.setAuditBy(SecurityUtils.getUsername());
            userService.updateUser(user);
        }
        return success();
    }

    @PreAuthorize("@ss.hasPermi('biz:audit:approve')")
    @PutMapping("/disable/{recordId}")
    public AjaxResult disable(@PathVariable Long recordId)
    {
        EeAuditRecord auditRecord = auditRecordMapper.selectAuditById(recordId);
        if (auditRecord != null && auditRecord.getUserId() != null)
        {
            SysUser user = new SysUser();
            user.setUserId(auditRecord.getUserId());
            user.setStatus("1");
            userService.updateUser(user);

            EeAuditRecord updateRecord = new EeAuditRecord();
            updateRecord.setRecordId(recordId);
            updateRecord.setAuditStatus("2");
            updateRecord.setAuditBy(SecurityUtils.getUserId());
            updateRecord.setAuditByName(SecurityUtils.getUsername());
            auditRecordMapper.updateAuditStatus(updateRecord);
        }
        return success();
    }

    @PreAuthorize("@ss.hasPermi('biz:audit:approve')")
    @PutMapping("/enable/{recordId}")
    public AjaxResult enable(@PathVariable Long recordId)
    {
        EeAuditRecord auditRecord = auditRecordMapper.selectAuditById(recordId);
        if (auditRecord != null && auditRecord.getUserId() != null)
        {
            SysUser user = new SysUser();
            user.setUserId(auditRecord.getUserId());
            user.setStatus("0");
            userService.updateUser(user);

            EeAuditRecord updateRecord = new EeAuditRecord();
            updateRecord.setRecordId(recordId);
            updateRecord.setAuditStatus("1");
            updateRecord.setAuditBy(SecurityUtils.getUserId());
            updateRecord.setAuditByName(SecurityUtils.getUsername());
            auditRecordMapper.updateAuditStatus(updateRecord);
        }
        return success();
    }

    @PreAuthorize("@ss.hasPermi('biz:audit:approve')")
    @DeleteMapping("/{recordId}")
    public AjaxResult remove(@PathVariable Long recordId)
    {
        auditRecordMapper.deleteAuditById(recordId);
        return success();
    }
}