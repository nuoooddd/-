package com.exemptenjoy.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.exemptenjoy.system.domain.EeAuditLog;
import com.exemptenjoy.system.mapper.EeAuditLogMapper;
import com.exemptenjoy.system.service.IEeAuditLogService;

@Service
public class EeAuditLogServiceImpl implements IEeAuditLogService {

    @Autowired
    private EeAuditLogMapper eeAuditLogMapper;

    @Override
    public EeAuditLog selectEeAuditLogByLogId(Long logId) {
        return eeAuditLogMapper.selectEeAuditLogByLogId(logId);
    }

    @Override
    public List<EeAuditLog> selectEeAuditLogList(EeAuditLog eeAuditLog) {
        return eeAuditLogMapper.selectEeAuditLogList(eeAuditLog);
    }

    @Override
    public int insertEeAuditLog(EeAuditLog eeAuditLog) {
        return eeAuditLogMapper.insertEeAuditLog(eeAuditLog);
    }

    @Override
    public void log(Long userId, String userName, String operation, String module, Long targetId, String detail, String ip) {
        EeAuditLog log = new EeAuditLog();
        log.setUserId(userId);
        log.setUserName(userName);
        log.setOperation(operation);
        log.setModule(module);
        log.setTargetId(targetId);
        log.setDetail(detail);
        log.setIp(ip);
        eeAuditLogMapper.insertEeAuditLog(log);
    }
}