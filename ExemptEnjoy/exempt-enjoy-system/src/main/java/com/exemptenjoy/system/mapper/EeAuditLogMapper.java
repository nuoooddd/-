package com.exemptenjoy.system.mapper;
import java.util.List;
import com.exemptenjoy.system.domain.EeAuditLog;
public interface EeAuditLogMapper {
    public EeAuditLog selectEeAuditLogByLogId(Long logId);
    public List<EeAuditLog> selectEeAuditLogList(EeAuditLog eeAuditLog);
    public int insertEeAuditLog(EeAuditLog eeAuditLog);
}