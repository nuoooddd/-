package com.exemptenjoy.system.service;
import java.util.List;
import com.exemptenjoy.system.domain.EeAuditLog;
public interface IEeAuditLogService {
    public EeAuditLog selectEeAuditLogByLogId(Long logId);
    public List<EeAuditLog> selectEeAuditLogList(EeAuditLog eeAuditLog);
    public int insertEeAuditLog(EeAuditLog eeAuditLog);
    public void log(Long userId, String userName, String operation, String module, Long targetId, String detail, String ip);
}